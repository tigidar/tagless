package tags.setters

trait Setter[Value, DomValue] {
  def domValue: DomValue
}
