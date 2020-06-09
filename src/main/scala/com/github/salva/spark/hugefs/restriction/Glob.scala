package com.github.salva.spark.hugefs.restriction

import java.util.regex.Pattern

import com.github.salva.spark.hugefs.{Restriction, Verdict}
import org.apache.zookeeper.KeeperException.BadArgumentsException

import scala.annotation.tailrec

class Glob(globs:Seq[String], caseInsensitive:Boolean) extends Restriction {
  if (globs.isEmpty) throw new IllegalArgumentException("At least one glob pattern is required")
  val patternPairs:Seq[PatternPair] = globs.flatMap(compileGlob(_, caseInsensitive))

  def this(glob:String, caseInsensitive:Boolean) = this(Seq(glob), caseInsensitive)

  case class PatternPair(goodPattern:Pattern, livePattern:Pattern) {
    def evaluate(path:String):Verdict = Verdict(goodPattern.matcher(path).matches,
                                                livePattern.matcher(path).matches)
  }

  override def evaluateCheap(path:String):Verdict = patternPairs.map(_.evaluate(path)).reduce(_||_)

  def stringAtoms(str:String):List[String] = {
    @tailrec
    def doIt(str: String, i:Int, soFar:List[String]): List[String] = {
      if (i >= str.length) soFar
      else {
        if (str.charAt(i) == '\\') doIt(str, i + 2, str.substring(i, i + 2) :: soFar)
        else doIt(str, i + 1, str.substring(i, i + 1) :: soFar)
      }
    }

    doIt(str, 0, Nil).reverse
  }

  def splitAll(atoms:List[String], breaker:String):List[List[String]] = {
    @tailrec
    def doIt(atoms:List[String], breaker:String,
             current:List[String], soFar:List[List[String]]):List[List[String]] = {
      atoms match {
        case Nil => current.reverse::soFar
        case atom::rest => {
          if (atom == breaker) doIt(rest, breaker, Nil, current.reverse::soFar)
          else doIt(rest, breaker, atom :: current, soFar)
        }
      }
    }
    doIt(atoms, breaker, Nil, Nil).reverse
  }

  def expandCurlyBrackets(atoms:List[String]) : List[List[String]] = {
    def split(atoms:List[String], breaker:String, fromTheEnd:Boolean=false): Option[(List[String], List[String])] = {
      val ix = if (fromTheEnd) atoms.lastIndexOf(breaker) else atoms.indexOf(breaker)
      if (ix >= 0) {
        var (start, end) = atoms.splitAt(ix)
        new Some(start, end.tail)
      }
      else None
    }

    split(atoms, "}") match {
      case Some((before, end)) => {
        split(before, "{", true) match {
          case Some((start, middle)) => {
            if (middle.isEmpty) expandCurlyBrackets(start ++ end)
            else splitAll(middle, ",").map(start ++ _ ++ end).flatMap(a => expandCurlyBrackets(a))
          }
          case None => throw new BadArgumentsException("Bad pattern, unmatched curly bracket '}'")
        }
      }
      case None => {
        if (atoms.indexOf("{") < 0) List(atoms)
        else throw new BadArgumentsException("Bad pattern, unmatched curly bracket '{'")
      }
    }
  }

  def globParts(globAtoms:List[String]):List[List[String]] = splitAll(globAtoms, "/")

  def partToRegex(atoms:List[String]):String = {

    @tailrec
    def advance(atoms:List[String], state:String, negated:Boolean, current:List[String], stack:List[String]):String = {

      def stackMerge(stack: List[String]) = stack.reverse.mkString

      def escapeAtom(atom: String): String = {
        if ((atom.length == 1) && ("/*!@#$%^&*()\"{}_[]|\\?/<>,.-".indexOf(atom) >= 0)) "\\" + atom else atom
      }

      def compileRangeToRegex(atoms: List[String], negated: Boolean) = (if (negated) "[^" else "[") + atoms.mkString + "]"

      state match {
        case "[" => { // inside a range, at the beginning
          atoms match {
            case "!" :: tail if !negated => advance(tail, "[", true, current, stack)
            case "]" :: tail => advance(tail, "[1", negated, escapeAtom("]") :: current, stack)
            case _ => advance(atoms, "[1", negated, current, stack)
          }
        }
        case "[1" => { // inside a range after at least one character has been consumed
          atoms match {
            case "]" :: tail => advance(tail, "a", false, Nil,
              compileRangeToRegex(current.reverse, negated) :: stack)
            case atomA :: "-" :: atomB :: tail if (atomB != "]") =>
              advance(tail, "[1", negated, escapeAtom(atomB) :: "-" :: escapeAtom(atomA) :: current, stack)
            case atom :: tail => advance(tail, "[1", negated, escapeAtom(atom) :: current, stack)
            case Nil => throw new IllegalArgumentException("Bad pattern, unmatched square bracket '['")
          }
        }
        case "a" => { // inside a literal sequence
          atoms match {
            case Nil => stackMerge(current.reverse.mkString :: stack)
            case "*" :: tail => advance(tail, "a", false, Nil, "[^/]*" :: current.reverse.mkString :: stack)
            case "?" :: tail => advance(tail, "a", false, Nil, "[^/]" :: current.reverse.mkString :: stack)
            case "]" :: tail => throw new IllegalArgumentException("Bad pattern, unmatched square bracket ']'")
            case "[" :: tail => advance(tail, "[", false, Nil, current.reverse.mkString :: stack)
            case atom :: tail => advance(tail, "a", false, escapeAtom(atom) :: current, stack)
          }
        }
      }
    }
    atoms match {
      case "*"::"*"::Nil => "**"
      case _ => advance(atoms, "a", false, Nil, Nil)
    }
  }

  def compileGlob(str: String, caseInsensitive:Boolean):Seq[PatternPair] = {
    def compileOne(atoms: List[String], caseInsensitive:Boolean): PatternPair = {

      def goodPattern(parts:List[String], caseInsensitive:Boolean): Pattern = {

        def reduce(acu: String, part: String): String = {
          part match {
            case "**" => {
              if (acu == "") ".*"
              else acu + "(?:/.*)?"
            }
            case _ => {
              if (acu == "") part
              else acu + "/" + part
            }
          }
        }

        Pattern.compile(parts.fold("")(reduce(_, _)), if (caseInsensitive) Pattern.CASE_INSENSITIVE else 0)
      }

      def livePattern(parts:List[String], caseInsensitive:Boolean): Pattern = {
        val ix = parts.indexOf("**")
        val hasAA = ix >= 0
        val parts1 = if (hasAA) parts.take(ix) else parts

        def reduce(part: String, right: String) = {
          if (right == "") part + (if (hasAA) "(?:/.*)?" else "(?!)")
          else part + "(?:/" + right + ")?"
        }

        val regex = if (parts1.isEmpty) {
          if (hasAA) ".*" else ""
        }
        else "(?:" + parts1.foldRight("")(reduce(_, _)) + ")?"
        Pattern.compile(regex, if (caseInsensitive) Pattern.CASE_INSENSITIVE else 0)
      }

      val parts = splitAll(atoms, "/").filter(_.nonEmpty).map(partToRegex(_))
      PatternPair(goodPattern(parts, caseInsensitive), livePattern(parts, caseInsensitive))
    }

    expandCurlyBrackets(stringAtoms(str)).map(compileOne(_, caseInsensitive))
  }
}
