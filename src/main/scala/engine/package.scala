/**
  * Created by FM on 05.04.16.
  */
package object engine {

  // From an outside perspective we only want to pass in anonymous data

  type Atom = core.Atom

  case class EngineAtom(name: String, arguments: Seq[String] = List())


  case class StreamEntry(time: Time, atoms: Set[Atom])

  type Stream = Set[StreamEntry]

}


