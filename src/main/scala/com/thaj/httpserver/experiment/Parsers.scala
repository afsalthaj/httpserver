/* WIP TO GET THE JSON PARSER
package com.thaj.httpserver.parser

// In the process of writing a JSON parser
/**
 * Making ParseError a type argument lets the Parsers interface work for any representation of ParseError,
 * and making Parser[+_] a type parameter means that the interface works for any representation of Parser.
 * @tparam ParseError
 * @tparam Parser
 */
trait Parsers[ParseError, Parser[+_]] { self =>

  // Forget about these things. Basically a string can be Parser[String] implicitly and get the functions
  // defined in Ops. With these two functions, Scala will automatically promote a String to a Parser,
  // and we get infix operators for any type that can be converted to a Parser[String].
  // However, We’ll follow the discipline of keeping the primary definition directly in
  // Parsers and delegating in ParserOps to this primary definition
  implicit def string(c: String): Parser[String]
  implicit def operators[A](p: Parser[A]) = ParserOps[A](p)
  implicit def asStringParser[A](a: A)(implicit f: A => Parser[String]): ParserOps[String] =
    ParserOps(f(a))

  // What if we want to recognize either the string "abra"or the string "cadabra"?
  def orString(s1: String, s2: String): Parser[String]
  // Making the above representation polymorphic
  def or[A](s1: Parser[A], s2: Parser[A]): Parser[A]
  // We could add a very specialized combinator for it:
  def run[A](parser: Parser[A])(input: String): Either[ParseError, A]

  // We can now recognize various strings, but we don’t have a way of talking about repetition.
  // For instance, how would we recognize three repetitions of our "abra" | "cadabra" parser?
  // Once again, let’s add a combinator for it
  def listOfN[A](n: Int, p: Parser[A]): Parser[List[A]]

  // Parser that represents zero or more 'a' characters
  // It’ll accumulate the results of all successful runs of p into a list.
  // As soon as p fails, the parser returns the empty List.
  // Exercise 9.3
  def many[A](p: Parser[A]): Parser[List[A]] = {
    map2(p, many(p))(_ :: _) or succeed(List[A]())
  }

  // lets define a map so that once we get the result we can calculate its size
  // It was primitive until we defined flatMap`
  def map[A, B](p: Parser[A])(f: A => B): Parser[B] = {

  }

  // Parser that returns the number of 'a' characters in it. This is awesome when I had map combinator
  def nMany[A](p: Parser[A]): Parser[Int] = p.many.map(_.size)

  // char
  val numA: Parser[Int] = char('a').many.map(_.size)

  def char(c: Char): Parser[Char] =
    string(c.toString).map(_.charAt(0))

  // This parser always succeeds with the value a, regardless of the input string
  // (since string("") will always succeed, even if the input is empty
  def succeed[A](c: A): Parser[A] =
    string(" ").map(_ => c)

  def slice[A](p: Parser[A]): Parser[String]

  // One ore more character 'a'
  def many1[A](p: Parser[A]): Parser[List[A]]

  // It feels like many1 shouldn’t have to be primitive, but should be defined
  // somehow in terms of many. Really, many1(p) is just p followed by many(p).
  // So it seems we need some way of running one parser, followed by another, assuming the first is successful. Let’s add that:
  def product[A, B](p: Parser[A], p2: => Parser[B]): Parser[(A, B)] =
    flatMap(p)(a => map(p2)(b => (a, b)))

  // Can you see how this signature implies an ability to sequence parsers where each
  // parser in the chain depends on the output of the previous one?
  def flatMap[A, B](p: Parser[A])(f: A => Parser[B]): Parser[B]

  // Exercise 9.1
  // TO avoid unnecessary expansions.
  //We’ll assume that or is left-biased, meaning it tries p1 on the input,
  // and then tries p2 only if p1 fails.9 In this case, we ought to make it non-strict
  // in its second argument, which may never even be consulted:
  def map2[A, B, C](p: Parser[A], p2: => Parser[B])(f: (A, B) => C): Parser[C] = {
    for { a <- p; b <- p2 } yield f(a, b)
  }

  def many1[A](p: Parser[A]): Parser[List[A]] =
    map2(p, many(p))(_ :: _)

  // With many1, we can now implement the parser for zero or more 'a' followed by one
  //  or more 'b' as follows:
  //  char('a').many.slice.map(_.size) ** char('b').many1.slice.map(_.size)

  // Exercise 9.4
  // Hard: Using map2 and succeed, implement the listOfN combinator from earlier.
  def listOfN[A](n: Int, p: Parser[A]): Parser[List[A]] = {
    if (n <= 0) succeed(List())
    else map2(p, listOfN(n - 1, p))(_ :: _)
  }

  object Law {
    private val charLaw =
      (c: Char) => run(char(c))(c.toString) == Right(c)

    private val stringLaw =
      (c: String) => run(string(c))(c) == Right(c)

    private val orLawForString1 =
      (s1: String, s2: String) => run(or(string(s1), string(s2)))(s1) == Right(s1)

    private val orLawForString2 =
      (s1: String, s2: String) => run(or(string(s1), string(s2)))(s1) == Right(s1)

    // Examples of listOfN
    run(listOfN(3, "ab" | "cad"))("ababcad") == Right("ababcad")
    run(listOfN(3, "ab" | "cad"))("cadabab") == Right("cadabab")
    run(listOfN(3, "ab" | "cad"))("ababab") == Right("ababab")

    // We call this combinator slice since we intend for it to return the
    // portion of the input string examined by the parser if successful.
    // we ignore the list accumulated by many and simply return the portion of the input string matched by the parser.
    run(slice(or(char('a'), char('b'))).many)("aaabba") == Right("aaaba")
    /**
     * even if the parser p.many.map(_.size) will generate an intermediate list when run, slice(p.many).map(_.size) will not.
     * This is a strong hint that slice is primi- tive, since it will have to have access to the internal representation of the parser.
     */

    run(numA)("aaa") == Right(3)
  }

  // We have got some nice non-empty repetition as well as combinators
  case class ParserOps[A](p: Parser[A]) {
    def |[B >: A](p2: Parser[B]): Parser[B] = self.or(p, p2)
    def or[B >: A](p2: => Parser[B]): Parser[B] = self.or(p, p2)
    def map[B](f: A => B): Parser[B] = self.map(p)(f)
    def many: Parser[List[A]] = self.many(p)
  }
}*/
