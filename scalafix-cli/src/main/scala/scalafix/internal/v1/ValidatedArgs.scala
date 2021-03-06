package scalafix.internal.v1

import scala.meta.internal.io.FileIO
import scala.meta.io.RelativePath
import scala.meta.Input
import scala.meta.AbsolutePath
import scala.meta.Classpath
import scala.meta.Source
import scala.meta.parsers.Parsed
import scalafix.internal.config.ScalafixConfig
import scalafix.internal.diff.DiffDisable
import scalafix.internal.util.SymbolTable

case class ValidatedArgs(
    args: Args,
    symtab: SymbolTable,
    rules: Rules,
    config: ScalafixConfig,
    classpath: Classpath,
    sourceroot: AbsolutePath,
    pathReplace: AbsolutePath => AbsolutePath,
    diffDisable: DiffDisable
) {

  def input(file: AbsolutePath): Input = {
    Input.VirtualFile(file.toString(), FileIO.slurp(file, args.charset))
  }

  def parse(input: Input): Parsed[Source] = {
    import scala.meta._
    val dialect = config.parser.dialectForFile(input.syntax)
    dialect(input).parse[Source]
  }

  def matches(path: RelativePath): Boolean =
    Args.baseMatcher.matches(path.toNIO) && {
      args.exclude.forall(!_.matches(path.toNIO))
    }

}
