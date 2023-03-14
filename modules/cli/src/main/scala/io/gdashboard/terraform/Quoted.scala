package io.gdashboard.terraform

final class Quoted private (val value: String)

object Quoted {
  def apply(value: String): Quoted = new Quoted(quote(value))

  private def quote(string: String) = "\"" + string.replace("\"", "'") + "\""
}
