package tags

import zio.test._
import zio.test.Assertion._

object SpecRunner extends ZIOSpecDefault {
  def spec = TagsSpec.spec
}

