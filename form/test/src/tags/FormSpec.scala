package tags

import munit.FunSuite
import _root_.html.lib.form.*
import tags.TestI18n.{given, *}

class FormSpec extends FunSuite:

  // ============================================================
  // FieldType sealed trait
  // ============================================================

  test("FieldType has all 14 expected case objects"):
    // Since FieldType is now a sealed trait, we verify all 14 types exist
    val types: List[FieldType] = List(
      FieldType.Text,
      FieldType.Area,
      FieldType.Number,
      FieldType.Email,
      FieldType.Password,
      FieldType.Hidden,
      FieldType.Tel,
      FieldType.Url,
      FieldType.Search,
      FieldType.Date,
      FieldType.Time,
      FieldType.Color,
      FieldType.Range,
      FieldType.File
    )
    assertEquals(types.length, 14)

  // ============================================================
  // String extensions — original field types
  // ============================================================

  test(".txt creates Text field"):
    val f = "Name".i18n.txt
    assertEquals(f, Field("Name".i18n, FieldType.Text))

  test(".area creates Area field"):
    val f = "Bio".i18n.area
    assertEquals(f, Field("Bio".i18n, FieldType.Area))

  test(".num creates Number field"):
    val f = "Age".i18n.num
    assertEquals(f, Field("Age".i18n, FieldType.Number))

  test(".email creates Email field"):
    val f = "Contact".i18n.email
    assertEquals(f, Field("Contact".i18n, FieldType.Email))

  test(".pwd creates Password field"):
    val f = "Secret".i18n.pwd
    assertEquals(f, Field("Secret".i18n, FieldType.Password))

  // ============================================================
  // String extensions — new field types
  // ============================================================

  test(".tel creates Tel field"):
    val f = "Phone".i18n.tel
    assertEquals(f, Field("Phone".i18n, FieldType.Tel))

  test(".url creates Url field"):
    val f = "Website".i18n.url
    assertEquals(f, Field("Website".i18n, FieldType.Url))

  test(".search creates Search field"):
    val f = "Query".i18n.search
    assertEquals(f, Field("Query".i18n, FieldType.Search))

  test(".date creates Date field"):
    val f = "Birthday".i18n.date
    assertEquals(f, Field("Birthday".i18n, FieldType.Date))

  test(".time creates Time field"):
    val f = "Start".i18n.time
    assertEquals(f, Field("Start".i18n, FieldType.Time))

  test(".color creates Color field"):
    val f = "Theme".i18n.color
    assertEquals(f, Field("Theme".i18n, FieldType.Color))

  test(".range creates Range field"):
    val f = "Volume".i18n.range
    assertEquals(f, Field("Volume".i18n, FieldType.Range))

  test(".file creates File field"):
    val f = "Upload".i18n.file
    assertEquals(f, Field("Upload".i18n, FieldType.File))

  // ============================================================
  // Field preserves label
  // ============================================================

  test("Field preserves label with spaces"):
    val f = "First Name".i18n.txt
    assertEquals(f.label, "First Name".i18n)

  test("Field preserves empty label"):
    val f = "".i18n.txt
    assertEquals(f.label, "".i18n)

  // ============================================================
  // SelectBuilder — ? operator
  // ============================================================

  test("I18n ? I18n creates SelectBuilder"):
    val sb = "Country".i18n ? "USA".i18n
    assertEquals(sb, SelectBuilder("Country".i18n, Vector("USA".i18n)))

  test("SelectBuilder ? chains options"):
    val sb = "Country".i18n ? "USA".i18n ? "Canada".i18n ? "UK".i18n
    assertEquals(
      sb,
      SelectBuilder(
        "Country".i18n,
        Vector("USA".i18n, "Canada".i18n, "UK".i18n)
      )
    )

  test("SelectBuilder preserves label"):
    val sb = "Favorite Color".i18n ? "Red".i18n ? "Blue".i18n
    assertEquals(sb.label, "Favorite Color".i18n)

  test("SelectBuilder preserves option order"):
    val sb = "Size".i18n ? "S".i18n ? "M".i18n ? "L".i18n ? "XL".i18n
    assertEquals(sb.options, Vector("S".i18n, "M".i18n, "L".i18n, "XL".i18n))

  test("SelectBuilder with single option"):
    val sb = "Binary".i18n ? "Yes".i18n
    assertEquals(sb.options, Vector("Yes".i18n))

  // ============================================================
  // FormEntry enum
  // ============================================================

  test("FormEntry.Input wraps Field"):
    val entry = FormEntry.Input(Field("Name".i18n, FieldType.Text))
    entry match
      case FormEntry.Input(f) => assertEquals(f.label, "Name".i18n)
      case _                  => fail("Expected FormEntry.Input")

  test("FieldAttrs default values"):
    val attrs = FieldAttrs()
    assertEquals(attrs.required, false)
    assertEquals(attrs.pattern, None)
    assertEquals(attrs.min, None)
    assertEquals(attrs.max, None)
    assertEquals(attrs.placeholder, None)

  test("FormEntry.Select wraps SelectField"):
    val entry =
      FormEntry.Select(SelectField("Country".i18n, Vector("A".i18n, "B".i18n)))
    entry match
      case FormEntry.Select(sf) =>
        assertEquals(sf.label, "Country".i18n)
        assertEquals(sf.options, Vector("A".i18n, "B".i18n))
      case _ => fail("Expected FormEntry.Select")

  test("FormEntry.Checkbox wraps CheckboxGroup"):
    val entry = FormEntry.Checkbox(
      CheckboxGroup("Prefs".i18n, Vector("X".i18n, "Y".i18n))
    )
    entry match
      case FormEntry.Checkbox(cg) =>
        assertEquals(cg.label, "Prefs".i18n)
        assertEquals(cg.options, Vector("X".i18n, "Y".i18n))
      case _ => fail("Expected FormEntry.Checkbox")

  // ============================================================
  // Form.Init — factory
  // ============================================================

  test("Form(prefix) creates Init"):
    val f = Form("test")
    assertEquals(f, Form.Init("test"))
    assertEquals(f.prefix, "test")

  // ============================================================
  // Form.Init |> Field — transitions to Fields
  // ============================================================

  test("Init |> Field transitions to Fields"):
    val f = Form("t") |> "Name".i18n.txt
    assertEquals(f.prefix, "t")
    assertEquals(f.fields.length, 1)
    assertEquals(
      f.fields.head,
      FormEntry.Input(Field("Name".i18n, FieldType.Text))
    )

  test("Init |> works with all original field types"):
    val fields = List(
      "a".i18n.txt,
      "b".i18n.area,
      "c".i18n.num,
      "d".i18n.email,
      "e".i18n.pwd
    )
    for field <- fields do
      val f = Form("t") |> field
      assertEquals(f.fields.length, 1)

  test("Init |> works with all new field types"):
    val fields = List(
      "a".i18n.tel,
      "b".i18n.url,
      "c".i18n.search,
      "d".i18n.date,
      "e".i18n.time,
      "f".i18n.color,
      "g".i18n.range,
      "h".i18n.file
    )
    for field <- fields do
      val f = Form("t") |> field
      assertEquals(f.fields.length, 1)

  // ============================================================
  // Form.Fields |> Field — accumulates
  // ============================================================

  test("Fields |> appends field"):
    val f = Form("t") |> "A".i18n.txt |> "B".i18n.email
    assertEquals(f.fields.length, 2)
    assertEquals(f.fields(0), FormEntry.Input(Field("A".i18n, FieldType.Text)))
    assertEquals(f.fields(1), FormEntry.Input(Field("B".i18n, FieldType.Email)))

  test("Fields |> preserves prefix"):
    val f = Form("myform") |> "A".i18n.txt |> "B".i18n.txt
    assertEquals(f.prefix, "myform")

  test("chaining many fields"):
    val f = Form("t")
      .|>("a".i18n.txt)
      .|>("b".i18n.email)
      .|>("c".i18n.num)
      .|>("d".i18n.pwd)
      .|>("e".i18n.area)
      .|>("f".i18n.tel)
      .|>("g".i18n.url)
      .|>("h".i18n.search)
      .|>("i".i18n.date)
      .|>("j".i18n.time)
      .|>("k".i18n.color)
      .|>("l".i18n.range)
      .|>("m".i18n.file)
    assertEquals(f.fields.length, 13)

  // ============================================================
  // Form.Init || SelectBuilder — select from init
  // ============================================================

  test("Init || creates select entry"):
    val f = Form("t") || ("Country".i18n ? "USA".i18n ? "UK".i18n)
    assertEquals(f.fields.length, 1)
    f.fields.head match
      case FormEntry.Select(sf) =>
        assertEquals(sf.label, "Country".i18n)
        assertEquals(sf.options, Vector("USA".i18n, "UK".i18n))
      case other => fail(s"Expected FormEntry.Select, got $other")

  // ============================================================
  // Form.Fields || SelectBuilder — select after fields
  // ============================================================

  test("Fields || appends select entry"):
    val f =
      Form("t").|>("Name".i18n.txt) || ("Color".i18n ? "Red".i18n ? "Blue".i18n)
    assertEquals(f.fields.length, 2)
    assertEquals(
      f.fields(0),
      FormEntry.Input(Field("Name".i18n, FieldType.Text))
    )
    f.fields(1) match
      case FormEntry.Select(sf) =>
        assertEquals(sf.label, "Color".i18n)
        assertEquals(sf.options, Vector("Red".i18n, "Blue".i18n))
      case other => fail(s"Expected FormEntry.Select, got $other")

  test("multiple selects"):
    val f = Form("t")
      .||("A".i18n ? "1".i18n ? "2".i18n)
      .||("B".i18n ? "3".i18n ? "4".i18n)
    assertEquals(f.fields.length, 2)

  // ============================================================
  // Form.Init |* SelectBuilder — checkbox from init
  // ============================================================

  test("Init |* creates checkbox entry"):
    val f = Form("t") |* ("Prefs".i18n ? "A".i18n ? "B".i18n)
    assertEquals(f.fields.length, 1)
    f.fields.head match
      case FormEntry.Checkbox(cg) =>
        assertEquals(cg.label, "Prefs".i18n)
        assertEquals(cg.options, Vector("A".i18n, "B".i18n))
      case other => fail(s"Expected FormEntry.Checkbox, got $other")

  // ============================================================
  // Form.Fields |* SelectBuilder — checkbox after fields
  // ============================================================

  test("Fields |* appends checkbox entry"):
    val f = Form("t").|>(
      "Name".i18n.txt
    ) |* ("Prefs".i18n ? "X".i18n ? "Y".i18n ? "Z".i18n)
    assertEquals(f.fields.length, 2)
    f.fields(1) match
      case FormEntry.Checkbox(cg) =>
        assertEquals(cg.label, "Prefs".i18n)
        assertEquals(cg.options, Vector("X".i18n, "Y".i18n, "Z".i18n))
      case other => fail(s"Expected FormEntry.Checkbox, got $other")

  test("multiple checkbox groups"):
    val f = Form("t")
      .|*("A".i18n ? "1".i18n ? "2".i18n)
      .|*("B".i18n ? "3".i18n ? "4".i18n ? "5".i18n)
    assertEquals(f.fields.length, 2)

  // ============================================================
  // Form.Fields |! — finalize to Done
  // ============================================================

  test("|! creates Done with submit label"):
    val f = Form("t").|>("Name".i18n.txt) |! "Submit".i18n
    f match
      case d: Form.Done =>
        assertEquals(d.prefix, "t")
        assertEquals(d.submitLabel, "Submit".i18n)
        assertEquals(d.fields.length, 1)
      case other => fail(s"Expected Form.Done, got $other")

  test("|! preserves all fields"):
    val f = Form("t")
      .|>("Name".i18n.txt)
      .|>("Email".i18n.email)
      .||("Country".i18n ? "A".i18n ? "B".i18n)
      .|*("Prefs".i18n ? "X".i18n ? "Y".i18n)
      .|!("Go".i18n)
    f match
      case d: Form.Done =>
        assertEquals(d.fields.length, 4)
        assertEquals(d.submitLabel, "Go".i18n)
      case other => fail(s"Expected Form.Done, got $other")

  // ============================================================
  // Mixed operator chaining
  // ============================================================

  test("full form with all element types"):
    val f = Form("registration")
      .|>("Username".i18n.txt)
      .|>("Password".i18n.pwd)
      .|>("Email".i18n.email)
      .|>("Phone".i18n.tel)
      .|>("Website".i18n.url)
      .|>("Birth Date".i18n.date)
      .|>("Start Time".i18n.time)
      .|>("Favorite Color".i18n.color)
      .|>("Volume".i18n.range)
      .|>("Resume".i18n.file)
      .|>("Bio".i18n.area)
      .|>("Query".i18n.search)
      .||(
        "Country".i18n ? "USA".i18n ? "Canada".i18n ? "UK".i18n ? "Other".i18n
      )
      .|*("Preferences".i18n ? "Newsletter".i18n ? "SMS".i18n ? "Push".i18n)
      .|!("Register".i18n)

    f match
      case d: Form.Done =>
        assertEquals(d.prefix, "registration")
        assertEquals(d.fields.length, 14)
        assertEquals(d.submitLabel, "Register".i18n)

        // Verify field types in order
        assert(d.fields(0).isInstanceOf[FormEntry.Input])
        assert(d.fields(1).isInstanceOf[FormEntry.Input])
        assert(d.fields(10).isInstanceOf[FormEntry.Input])
        assert(d.fields(12).isInstanceOf[FormEntry.Select])
        assert(d.fields(13).isInstanceOf[FormEntry.Checkbox])
      case other => fail(s"Expected Form.Done, got $other")

  test("interleaved inputs, selects, and checkboxes"):
    val f = Form("t")
      .|>("A".i18n.txt)
      .||("S1".i18n ? "x".i18n ? "y".i18n)
      .|>("B".i18n.email)
      .|*("C1".i18n ? "p".i18n ? "q".i18n)
      .||("S2".i18n ? "a".i18n ? "b".i18n)
      .|!("Done".i18n)

    f match
      case d: Form.Done =>
        assertEquals(d.fields.length, 5)
        assert(d.fields(0).isInstanceOf[FormEntry.Input])
        assert(d.fields(1).isInstanceOf[FormEntry.Select])
        assert(d.fields(2).isInstanceOf[FormEntry.Input])
        assert(d.fields(3).isInstanceOf[FormEntry.Checkbox])
        assert(d.fields(4).isInstanceOf[FormEntry.Select])
      case other => fail(s"Expected Form.Done, got $other")

  // ============================================================
  // Prefix preserved through all transitions
  // ============================================================

  test("prefix preserved through Init -> Fields"):
    val f = Form("abc") |> "X".i18n.txt
    assertEquals(f.prefix, "abc")

  test("prefix preserved through Init -> Fields (select)"):
    val f = Form("abc") || ("X".i18n ? "1".i18n)
    assertEquals(f.prefix, "abc")

  test("prefix preserved through Init -> Fields (checkbox)"):
    val f = Form("abc") |* ("X".i18n ? "1".i18n)
    assertEquals(f.prefix, "abc")

  test("prefix preserved through Fields -> Done"):
    val d = Form("abc").|>("X".i18n.txt) |! "Go".i18n
    d match
      case done: Form.Done => assertEquals(done.prefix, "abc")
      case other           => fail(s"Expected Form.Done, got $other")

  // ============================================================
  // Operator precedence — ? binds tighter than |>, ||, |*
  // ============================================================

  test("? binds tighter than || in infix notation"):
    // This tests that "L" ? "A" ? "B" evaluates to SelectBuilder
    // before || sees it. We use dot-syntax to be explicit.
    val sb = "Label".i18n ? "Opt1".i18n ? "Opt2".i18n ? "Opt3".i18n
    assertEquals(sb.options.length, 3)
    val f = Form("t") || sb
    f.fields.head match
      case FormEntry.Select(sf) => assertEquals(sf.options.length, 3)
      case other                => fail(s"Expected Select, got $other")

  test("? binds tighter than |* in infix notation"):
    val sb = "Prefs".i18n ? "A".i18n ? "B".i18n
    assertEquals(sb.options.length, 2)
    val f = Form("t") |* sb
    f.fields.head match
      case FormEntry.Checkbox(cg) => assertEquals(cg.options.length, 2)
      case other                  => fail(s"Expected Checkbox, got $other")

  // ============================================================
  // Edge cases
  // ============================================================

  test("form with only a select"):
    val f = Form("t").||("Pick".i18n ? "A".i18n) |! "Go".i18n
    f match
      case d: Form.Done => assertEquals(d.fields.length, 1)
      case other        => fail(s"Expected Done, got $other")

  test("form with only a checkbox group"):
    val f = Form("t").|*("Pick".i18n ? "A".i18n) |! "Go".i18n
    f match
      case d: Form.Done => assertEquals(d.fields.length, 1)
      case other        => fail(s"Expected Done, got $other")

  test("select with many options"):
    val sb =
      "Lang".i18n ? "Scala".i18n ? "Kotlin".i18n ? "Java".i18n ? "Rust".i18n ? "Go".i18n ? "Python".i18n ? "TypeScript".i18n
    assertEquals(sb.options.length, 7)
    assertEquals(sb.label, "Lang".i18n)

  test("checkbox with many options"):
    val f = Form(
      "t"
    ) |* ("Langs".i18n ? "A".i18n ? "B".i18n ? "C".i18n ? "D".i18n ? "E".i18n) |! "Ok".i18n
    f match
      case d: Form.Done =>
        d.fields.head match
          case FormEntry.Checkbox(cg) => assertEquals(cg.options.length, 5)
          case other                  => fail(s"Expected Checkbox, got $other")
      case other => fail(s"Expected Done, got $other")

  test("each new field type roundtrips through FormEntry.Input"):
    val types = List(
      "a".i18n.tel -> FieldType.Tel,
      "b".i18n.url -> FieldType.Url,
      "c".i18n.search -> FieldType.Search,
      "d".i18n.date -> FieldType.Date,
      "e".i18n.time -> FieldType.Time,
      "f".i18n.color -> FieldType.Color,
      "g".i18n.range -> FieldType.Range,
      "h".i18n.file -> FieldType.File
    )
    for (field, expectedType) <- types do
      val entry = FormEntry.Input(field)
      entry match
        case FormEntry.Input(f) =>
          assertEquals(f.fieldType, expectedType)
        case other =>
          fail(s"Expected Input with $expectedType, got $other")

  // ============================================================
  // FormEntry.Fieldset ADT
  // ============================================================

  test("FormEntry.Fieldset wraps legend and entries"):
    val entries = Vector(
      FormEntry.Input(Field("Name".i18n, FieldType.Text)),
      FormEntry.Input(Field("Email".i18n, FieldType.Email))
    )
    val fs = FormEntry.Fieldset("Personal Info".i18n, entries)
    fs match
      case FormEntry.Fieldset(legend, es) =>
        assertEquals(legend, "Personal Info".i18n)
        assertEquals(es.length, 2)
      case other => fail(s"Expected Fieldset, got $other")

  test("FormEntry.Fieldset with empty entries"):
    val fs = FormEntry.Fieldset("Empty".i18n, Vector.empty)
    fs match
      case FormEntry.Fieldset(legend, es) =>
        assertEquals(legend, "Empty".i18n)
        assertEquals(es.length, 0)
      case other => fail(s"Expected Fieldset, got $other")

  // ============================================================
  // |>> from Init — enter fieldset
  // ============================================================

  test("Init |>> creates InFieldset"):
    val fs = Form("t") |>> "Personal Info".i18n
    assertEquals(fs.prefix, "t")
    assertEquals(fs.legend, "Personal Info".i18n)
    assertEquals(fs.outer, Vector.empty)
    assertEquals(fs.inner, Vector.empty)

  // ============================================================
  // |> inside fieldset — add fields
  // ============================================================

  test("InFieldset |> adds field to inner"):
    val fs = Form("t") |>> "Info".i18n |> "Name".i18n.txt
    assertEquals(fs.inner.length, 1)
    assertEquals(
      fs.inner.head,
      FormEntry.Input(Field("Name".i18n, FieldType.Text))
    )

  test("InFieldset |> chains multiple fields"):
    val fs = Form("t") |>> "Info".i18n |> "Name".i18n.txt |> "Email".i18n.email
    assertEquals(fs.inner.length, 2)
    assertEquals(
      fs.inner(0),
      FormEntry.Input(Field("Name".i18n, FieldType.Text))
    )
    assertEquals(
      fs.inner(1),
      FormEntry.Input(Field("Email".i18n, FieldType.Email))
    )

  // ============================================================
  // || inside fieldset — add select
  // ============================================================

  test("InFieldset || adds select to inner"):
    val fs =
      Form("t") |>> "Info".i18n || ("Country".i18n ? "USA".i18n ? "UK".i18n)
    assertEquals(fs.inner.length, 1)
    fs.inner.head match
      case FormEntry.Select(sf) =>
        assertEquals(sf.label, "Country".i18n)
        assertEquals(sf.options, Vector("USA".i18n, "UK".i18n))
      case other => fail(s"Expected Select, got $other")

  // ============================================================
  // |* inside fieldset — add checkbox group
  // ============================================================

  test("InFieldset |* adds checkbox to inner"):
    val fs =
      Form("t") |>> "Prefs".i18n |* ("Notify".i18n ? "Email".i18n ? "SMS".i18n)
    assertEquals(fs.inner.length, 1)
    fs.inner.head match
      case FormEntry.Checkbox(cg) =>
        assertEquals(cg.label, "Notify".i18n)
        assertEquals(cg.options, Vector("Email".i18n, "SMS".i18n))
      case other => fail(s"Expected Checkbox, got $other")

  // ============================================================
  // |<< — exit fieldset
  // ============================================================

  test("|<< exits fieldset and appends to outer"):
    val f =
      (Form("t") |>> "Info".i18n |> "Name".i18n.txt |> "Email".i18n.email).|<<
    assertEquals(f.prefix, "t")
    assertEquals(f.fields.length, 1)
    f.fields.head match
      case FormEntry.Fieldset(legend, entries) =>
        assertEquals(legend, "Info".i18n)
        assertEquals(entries.length, 2)
      case other => fail(s"Expected Fieldset, got $other")

  test("|<< on empty fieldset produces empty Fieldset entry"):
    val f = (Form("t") |>> "Empty".i18n).|<<
    assertEquals(f.fields.length, 1)
    f.fields.head match
      case FormEntry.Fieldset(legend, entries) =>
        assertEquals(legend, "Empty".i18n)
        assertEquals(entries.length, 0)
      case other => fail(s"Expected Fieldset, got $other")

  // ============================================================
  // |>> from Fields — enter fieldset after existing fields
  // ============================================================

  test("Fields |>> preserves prior entries"):
    val f = (Form(
      "t"
    ) |> "Before".i18n.txt |>> "Group".i18n |> "Inside".i18n.email).|<<
    assertEquals(f.fields.length, 2)
    assertEquals(
      f.fields(0),
      FormEntry.Input(Field("Before".i18n, FieldType.Text))
    )
    f.fields(1) match
      case FormEntry.Fieldset(legend, entries) =>
        assertEquals(legend, "Group".i18n)
        assertEquals(entries.length, 1)
      case other => fail(s"Expected Fieldset, got $other")

  // ============================================================
  // Multiple fieldsets
  // ============================================================

  test("multiple fieldsets in sequence"):
    val f = Form("t")
      .|>>("Personal".i18n)
      .|>("Name".i18n.txt)
      .|>("Email".i18n.email)
      .|<<
      .|>>("Preferences".i18n)
      .|>("Color".i18n.color)
      .|<<
    assertEquals(f.fields.length, 2)
    f.fields(0) match
      case FormEntry.Fieldset(legend, entries) =>
        assertEquals(legend.apply, "Personal")
        assertEquals(entries.length, 2)
      case other => fail(s"Expected first Fieldset, got $other")
    f.fields(1) match
      case FormEntry.Fieldset(legend, entries) =>
        assertEquals(legend.apply, "Preferences")
        assertEquals(entries.length, 1)
      case other => fail(s"Expected second Fieldset, got $other")

  test("fields before, between, and after fieldsets"):
    val f = Form("t")
      .|>("Top".i18n.txt)
      .|>>("Group1".i18n)
      .|>("G1".i18n.email)
      .|<<
      .|>("Middle".i18n.num)
      .|>>("Group2".i18n)
      .|>("G2".i18n.date)
      .|<<
      .|>("Bottom".i18n.tel)
    assertEquals(f.fields.length, 5)
    assert(f.fields(0).isInstanceOf[FormEntry.Input])
    assert(f.fields(1).isInstanceOf[FormEntry.Fieldset])
    assert(f.fields(2).isInstanceOf[FormEntry.Input])
    assert(f.fields(3).isInstanceOf[FormEntry.Fieldset])
    assert(f.fields(4).isInstanceOf[FormEntry.Input])

  // ============================================================
  // Mixed content inside fieldset
  // ============================================================

  test("fieldset with input, select, and checkbox"):
    val f = Form("t")
      .|>>("Mixed".i18n)
      .|>("Name".i18n.txt)
      .||("Role".i18n ? "Admin".i18n ? "User".i18n)
      .|*("Notify".i18n ? "Email".i18n ? "Push".i18n)
      .|<<
    assertEquals(f.fields.length, 1)
    f.fields.head match
      case FormEntry.Fieldset(_, entries) =>
        assertEquals(entries.length, 3)
        assert(entries(0).isInstanceOf[FormEntry.Input])
        assert(entries(1).isInstanceOf[FormEntry.Select])
        assert(entries(2).isInstanceOf[FormEntry.Checkbox])
      case other => fail(s"Expected Fieldset, got $other")

  // ============================================================
  // Full form with fieldsets and |!
  // ============================================================

  test("complete form with fieldsets and submit"):
    val f = Form("contact")
      .|>>("Personal Info".i18n)
      .|>("Name".i18n.txt)
      .|>("Email".i18n.email)
      .|<<
      .|>>("Preferences".i18n)
      .|>("Color".i18n.color)
      .||("Theme".i18n ? "Light".i18n ? "Dark".i18n)
      .|<<
      .|!("Submit".i18n)
    f match
      case d: Form.Done =>
        assertEquals(d.prefix, "contact")
        assertEquals(d.submitLabel, "Submit".i18n)
        assertEquals(d.fields.length, 2)
        d.fields(0) match
          case FormEntry.Fieldset(legend, entries) =>
            assertEquals(legend.apply, "Personal Info")
            assertEquals(entries.length, 2)
          case other => fail(s"Expected first Fieldset, got $other")
        d.fields(1) match
          case FormEntry.Fieldset(legend, entries) =>
            assertEquals(legend.apply, "Preferences")
            assertEquals(entries.length, 2)
          case other => fail(s"Expected second Fieldset, got $other")

  // ============================================================
  // Prefix preservation through fieldset operations
  // ============================================================

  test("prefix preserved through Init -> InFieldset"):
    val fs = Form("myprefix") |>> "Group".i18n
    assertEquals(fs.prefix, "myprefix")

  test("prefix preserved through InFieldset -> Fields (|<<)"):
    val f = (Form("myprefix") |>> "Group".i18n |> "X".i18n.txt).|<<
    assertEquals(f.prefix, "myprefix")

  test("prefix preserved through fieldset to Done"):
    val d =
      Form("myprefix").|>>("Group".i18n).|>("X".i18n.txt).|<<.|!("Go".i18n)
    d match
      case done: Form.Done => assertEquals(done.prefix, "myprefix")

  // ============================================================
  // Edge cases
  // ============================================================

  test("fieldset as only content before submit"):
    val d = Form("t").|>>("Only".i18n).|>("X".i18n.txt).|<<.|!("Go".i18n)
    d match
      case done: Form.Done =>
        assertEquals(done.fields.length, 1)
        assert(done.fields.head.isInstanceOf[FormEntry.Fieldset])

  test("many fieldsets"):
    val f = Form("t")
      .|>>("A".i18n)
      .|>("a".i18n.txt)
      .|<<
      .|>>("B".i18n)
      .|>("b".i18n.txt)
      .|<<
      .|>>("C".i18n)
      .|>("c".i18n.txt)
      .|<<
      .|>>("D".i18n)
      .|>("d".i18n.txt)
      .|<<
    assertEquals(f.fields.length, 4)
    for entry <- f.fields do assert(entry.isInstanceOf[FormEntry.Fieldset])

  // ============================================================
  // Validation attributes — .required
  // ============================================================

  test(".required works on Text field"):
    val f = "Name".i18n.txt.required
    assertEquals(f.attrs.required, true)
    assertEquals(f.label, "Name".i18n)
    assertEquals(f.fieldType, FieldType.Text)

  test(".required works on Email field"):
    val f = "Email".i18n.email.required
    assertEquals(f.attrs.required, true)

  test(".required works on Number field"):
    val f = "Age".i18n.num.required
    assertEquals(f.attrs.required, true)

  test(".required works on Password field"):
    val f = "Password".i18n.pwd.required
    assertEquals(f.attrs.required, true)

  test(".required works on Tel field"):
    val f = "Phone".i18n.tel.required
    assertEquals(f.attrs.required, true)

  test(".required works on Url field"):
    val f = "Website".i18n.url.required
    assertEquals(f.attrs.required, true)

  test(".required works on Search field"):
    val f = "Query".i18n.search.required
    assertEquals(f.attrs.required, true)

  test(".required works on Area field"):
    val f = "Bio".i18n.area.required
    assertEquals(f.attrs.required, true)

  test(".required works on Date field"):
    val f = "Birthday".i18n.date.required
    assertEquals(f.attrs.required, true)

  test(".required works on Time field"):
    val f = "Start".i18n.time.required
    assertEquals(f.attrs.required, true)

  test(".required works on File field"):
    val f = "Upload".i18n.file.required
    assertEquals(f.attrs.required, true)

  // ============================================================
  // Validation attributes — .pattern
  // ============================================================

  test(".pattern works on Text field"):
    val f = "Name".i18n.txt.pattern("[A-Za-z]+")
    assertEquals(f.attrs.pattern, Some("[A-Za-z]+"))

  test(".pattern works on Tel field"):
    val f = "Phone".i18n.tel.pattern("\\d{3}-\\d{4}")
    assertEquals(f.attrs.pattern, Some("\\d{3}-\\d{4}"))

  test(".pattern works on Email field"):
    val f = "Email".i18n.email.pattern(".+@.+\\..+")
    assertEquals(f.attrs.pattern, Some(".+@.+\\..+"))

  test(".pattern works on Url field"):
    val f = "Website".i18n.url.pattern("https?://.+")
    assertEquals(f.attrs.pattern, Some("https?://.+"))

  test(".pattern works on Search field"):
    val f = "Query".i18n.search.pattern("\\w+")
    assertEquals(f.attrs.pattern, Some("\\w+"))

  test(".pattern works on Password field"):
    val f = "Pwd".i18n.pwd.pattern("(?=.*[A-Z])(?=.*[0-9]).{8,}")
    assertEquals(f.attrs.pattern, Some("(?=.*[A-Z])(?=.*[0-9]).{8,}"))

  // ============================================================
  // Validation attributes — .min / .max
  // ============================================================

  test(".min works on Number field"):
    val f = "Age".i18n.num.min("0")
    assertEquals(f.attrs.min, Some("0"))

  test(".max works on Number field"):
    val f = "Age".i18n.num.max("120")
    assertEquals(f.attrs.max, Some("120"))

  test(".min and .max work together on Number field"):
    val f = "Age".i18n.num.min("18").max("65")
    assertEquals(f.attrs.min, Some("18"))
    assertEquals(f.attrs.max, Some("65"))

  test(".min works on Date field"):
    val f = "Start".i18n.date.min("2020-01-01")
    assertEquals(f.attrs.min, Some("2020-01-01"))

  test(".max works on Date field"):
    val f = "End".i18n.date.max("2030-12-31")
    assertEquals(f.attrs.max, Some("2030-12-31"))

  test(".min works on Time field"):
    val f = "Opening".i18n.time.min("09:00")
    assertEquals(f.attrs.min, Some("09:00"))

  test(".max works on Time field"):
    val f = "Closing".i18n.time.max("17:00")
    assertEquals(f.attrs.max, Some("17:00"))

  test(".min works on Range field"):
    val f = "Volume".i18n.range.min("0")
    assertEquals(f.attrs.min, Some("0"))

  test(".max works on Range field"):
    val f = "Volume".i18n.range.max("100")
    assertEquals(f.attrs.max, Some("100"))

  test(".min and .max work together on Range field"):
    val f = "Volume".i18n.range.min("0").max("10")
    assertEquals(f.attrs.min, Some("0"))
    assertEquals(f.attrs.max, Some("10"))

  // ============================================================
  // Validation attributes — .placeholder
  // ============================================================

  test(".placeholder works on Text field"):
    val f = "Name".i18n.txt.placeholder("Enter your name")
    assertEquals(f.attrs.placeholder, Some("Enter your name"))

  test(".placeholder works on Email field"):
    val f = "Email".i18n.email.placeholder("you@example.com")
    assertEquals(f.attrs.placeholder, Some("you@example.com"))

  test(".placeholder works on Tel field"):
    val f = "Phone".i18n.tel.placeholder("555-1234")
    assertEquals(f.attrs.placeholder, Some("555-1234"))

  test(".placeholder works on Url field"):
    val f = "Website".i18n.url.placeholder("https://example.com")
    assertEquals(f.attrs.placeholder, Some("https://example.com"))

  test(".placeholder works on Search field"):
    val f = "Query".i18n.search.placeholder("Search...")
    assertEquals(f.attrs.placeholder, Some("Search..."))

  test(".placeholder works on Password field"):
    val f = "Pwd".i18n.pwd.placeholder("Enter password")
    assertEquals(f.attrs.placeholder, Some("Enter password"))

  test(".placeholder works on Number field"):
    val f = "Age".i18n.num.placeholder("e.g., 25")
    assertEquals(f.attrs.placeholder, Some("e.g., 25"))

  test(".placeholder works on Area field"):
    val f = "Bio".i18n.area.placeholder("Tell us about yourself...")
    assertEquals(f.attrs.placeholder, Some("Tell us about yourself..."))

  // ============================================================
  // Chaining multiple validation attributes
  // ============================================================

  test("chaining .required and .placeholder on Text field"):
    val f = "Name".i18n.txt.required.placeholder("Enter name")
    assertEquals(f.attrs.required, true)
    assertEquals(f.attrs.placeholder, Some("Enter name"))

  test("chaining .required, .pattern, and .placeholder on Text field"):
    val f =
      "Name".i18n.txt.required.pattern("[A-Za-z]+").placeholder("Letters only")
    assertEquals(f.attrs.required, true)
    assertEquals(f.attrs.pattern, Some("[A-Za-z]+"))
    assertEquals(f.attrs.placeholder, Some("Letters only"))

  test("chaining .required, .min, .max, and .placeholder on Number field"):
    val f = "Age".i18n.num.required.min("18").max("65").placeholder("18-65")
    assertEquals(f.attrs.required, true)
    assertEquals(f.attrs.min, Some("18"))
    assertEquals(f.attrs.max, Some("65"))
    assertEquals(f.attrs.placeholder, Some("18-65"))

  test("chaining .required and .pattern on Email field"):
    val f = "Email".i18n.email.required.pattern(".+@company\\.com")
    assertEquals(f.attrs.required, true)
    assertEquals(f.attrs.pattern, Some(".+@company\\.com"))

  test("chaining in different orders produces same result"):
    val f1 = "Name".i18n.txt.required.placeholder("Name").pattern("[A-Z][a-z]+")
    val f2 = "Name".i18n.txt.pattern("[A-Z][a-z]+").placeholder("Name").required
    assertEquals(f1.attrs, f2.attrs)

  // ============================================================
  // Attrs preserved through FormEntry.Input
  // ============================================================

  test("attrs preserved when wrapped in FormEntry.Input"):
    val field = "Name".i18n.txt.required.placeholder("Your name")
    val entry = FormEntry.Input(field)
    entry match
      case FormEntry.Input(f) =>
        assertEquals(f.attrs.required, true)
        assertEquals(f.attrs.placeholder, Some("Your name"))
      case _ => fail("Expected FormEntry.Input")

  test("attrs with all properties preserved in FormEntry.Input"):
    val field = "Age".i18n.num.required.min("0").max("150").placeholder("Age")
    val entry = FormEntry.Input(field)
    entry match
      case FormEntry.Input(f) =>
        assertEquals(f.attrs.required, true)
        assertEquals(f.attrs.min, Some("0"))
        assertEquals(f.attrs.max, Some("150"))
        assertEquals(f.attrs.placeholder, Some("Age"))
      case _ => fail("Expected FormEntry.Input")

  // ============================================================
  // Attrs preserved through form operators
  // ============================================================

  test("attrs preserved through |> operator"):
    val f = Form("t") |> "Name".i18n.txt.required.placeholder("Enter name")
    assertEquals(f.fields.length, 1)
    f.fields.head match
      case FormEntry.Input(field) =>
        assertEquals(field.attrs.required, true)
        assertEquals(field.attrs.placeholder, Some("Enter name"))
      case _ => fail("Expected Input")

  test("attrs preserved through multiple |> operators"):
    val f = Form("t")
      .|>("Name".i18n.txt.required.pattern("[A-Za-z]+"))
      .|>("Age".i18n.num.min("0").max("150"))
      .|>("Email".i18n.email.required.placeholder("you@example.com"))
    assertEquals(f.fields.length, 3)

    f.fields(0) match
      case FormEntry.Input(field) =>
        assertEquals(field.attrs.required, true)
        assertEquals(field.attrs.pattern, Some("[A-Za-z]+"))
      case _ => fail("Expected Input")

    f.fields(1) match
      case FormEntry.Input(field) =>
        assertEquals(field.attrs.min, Some("0"))
        assertEquals(field.attrs.max, Some("150"))
      case _ => fail("Expected Input")

    f.fields(2) match
      case FormEntry.Input(field) =>
        assertEquals(field.attrs.required, true)
        assertEquals(field.attrs.placeholder, Some("you@example.com"))
      case _ => fail("Expected Input")

  test("attrs preserved through fieldset operators"):
    val f = (Form("t") |>> "Info".i18n |> "Name".i18n.txt.required.placeholder(
      "Name"
    )).|<<
    assertEquals(f.fields.length, 1)
    f.fields.head match
      case FormEntry.Fieldset(_, entries) =>
        assertEquals(entries.length, 1)
        entries.head match
          case FormEntry.Input(field) =>
            assertEquals(field.attrs.required, true)
            assertEquals(field.attrs.placeholder, Some("Name"))
          case _ => fail("Expected Input")
      case _ => fail("Expected Fieldset")

  test("attrs preserved through |! operator to Done"):
    val d = Form("t")
      .|>("Email".i18n.email.required.pattern(".+@.+"))
      .|!("Submit".i18n)
    d match
      case done: Form.Done =>
        assertEquals(done.fields.length, 1)
        done.fields.head match
          case FormEntry.Input(field) =>
            assertEquals(field.attrs.required, true)
            assertEquals(field.attrs.pattern, Some(".+@.+"))
          case _ => fail("Expected Input")
      case _ => fail("Expected Done")

  // ============================================================
  // Complete form with validation attributes
  // ============================================================

  test("complete registration form with validation attributes"):
    val f = Form("registration")
      .|>(
        "Username".i18n.txt.required
          .pattern("[a-z0-9_]{3,16}")
          .placeholder("username")
      )
      .|>(
        "Password".i18n.pwd.required
          .pattern("(?=.*[A-Z])(?=.*[0-9]).{8,}")
          .placeholder("Min 8 chars")
      )
      .|>("Email".i18n.email.required.placeholder("you@example.com"))
      .|>("Age".i18n.num.required.min("18").max("120").placeholder("18+"))
      .|>("Website".i18n.url.placeholder("https://..."))
      .|>("Bio".i18n.area.placeholder("Tell us about yourself..."))
      .|!("Register".i18n)

    f match
      case d: Form.Done =>
        assertEquals(d.fields.length, 6)
        assertEquals(d.submitLabel, "Register".i18n)

        // Check Username field
        d.fields(0) match
          case FormEntry.Input(field) =>
            assertEquals(field.label, "Username".i18n)
            assertEquals(field.attrs.required, true)
            assertEquals(field.attrs.pattern, Some("[a-z0-9_]{3,16}"))
            assertEquals(field.attrs.placeholder, Some("username"))
          case _ => fail("Expected Username Input")

        // Check Password field
        d.fields(1) match
          case FormEntry.Input(field) =>
            assertEquals(field.label, "Password".i18n)
            assertEquals(field.attrs.required, true)
            assertEquals(
              field.attrs.pattern,
              Some("(?=.*[A-Z])(?=.*[0-9]).{8,}")
            )
            assertEquals(field.attrs.placeholder, Some("Min 8 chars"))
          case _ => fail("Expected Password Input")

        // Check Email field
        d.fields(2) match
          case FormEntry.Input(field) =>
            assertEquals(field.label, "Email".i18n)
            assertEquals(field.attrs.required, true)
            assertEquals(field.attrs.placeholder, Some("you@example.com"))
          case _ => fail("Expected Email Input")

        // Check Age field
        d.fields(3) match
          case FormEntry.Input(field) =>
            assertEquals(field.label, "Age".i18n)
            assertEquals(field.attrs.required, true)
            assertEquals(field.attrs.min, Some("18"))
            assertEquals(field.attrs.max, Some("120"))
            assertEquals(field.attrs.placeholder, Some("18+"))
          case _ => fail("Expected Age Input")
      case _ => fail("Expected Done")

  // ============================================================
  // Compile-time type safety verification
  // ============================================================
  // The following combinations should NOT compile due to type constraints:
  //
  // "X".color.required        // Color doesn't have CanBeRequired
  // "X".hidden.required       // Hidden doesn't have CanBeRequired
  // "X".range.required        // Range doesn't have CanBeRequired
  //
  // "X".num.pattern("...")    // Number doesn't have HasPattern
  // "X".date.pattern("...")   // Date doesn't have HasPattern
  // "X".time.pattern("...")   // Time doesn't have HasPattern
  // "X".color.pattern("...")  // Color doesn't have HasPattern
  // "X".range.pattern("...")  // Range doesn't have HasPattern
  // "X".file.pattern("...")   // File doesn't have HasPattern
  // "X".area.pattern("...")   // Area doesn't have HasPattern
  // "X".hidden.pattern("...")  // Hidden doesn't have HasPattern
  //
  // "X".txt.min("0")          // Text doesn't have HasMinMax
  // "X".txt.max("10")         // Text doesn't have HasMinMax
  // "X".email.min("0")        // Email doesn't have HasMinMax
  // "X".tel.min("0")          // Tel doesn't have HasMinMax
  // "X".url.min("0")          // Url doesn't have HasMinMax
  // "X".search.min("0")       // Search doesn't have HasMinMax
  // "X".pwd.min("0")          // Password doesn't have HasMinMax
  // "X".area.min("0")         // Area doesn't have HasMinMax
  // "X".file.min("0")         // File doesn't have HasMinMax
  // "X".color.min("0")        // Color doesn't have HasMinMax
  // "X".hidden.min("0")       // Hidden doesn't have HasMinMax
  //
  // "X".date.placeholder("...") // Date doesn't have HasPlaceholder
  // "X".time.placeholder("...") // Time doesn't have HasPlaceholder
  // "X".color.placeholder("...") // Color doesn't have HasPlaceholder
  // "X".range.placeholder("...") // Range doesn't have HasPlaceholder
  // "X".file.placeholder("...") // File doesn't have HasPlaceholder (note: File has no placeholder)
  // "X".hidden.placeholder("...") // Hidden doesn't have HasPlaceholder

  test("type safety - fields with CanBeRequired can be marked required"):
    // These should all compile
    val t1 = "x".i18n.txt.required
    val t2 = "x".i18n.area.required
    val t3 = "x".i18n.num.required
    val t4 = "x".i18n.email.required
    val t5 = "x".i18n.pwd.required
    val t6 = "x".i18n.tel.required
    val t7 = "x".i18n.url.required
    val t8 = "x".i18n.search.required
    val t9 = "x".i18n.date.required
    val t10 = "x".i18n.time.required
    val t11 = "x".i18n.file.required
    // Verify they all have required = true
    assert(
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11).forall(
        _.attrs.required
      )
    )

  test("type safety - fields with HasPattern can use pattern"):
    // These should all compile
    val t1 = "x".i18n.txt.pattern(".*")
    val t2 = "x".i18n.tel.pattern(".*")
    val t3 = "x".i18n.email.pattern(".*")
    val t4 = "x".i18n.url.pattern(".*")
    val t5 = "x".i18n.search.pattern(".*")
    val t6 = "x".i18n.pwd.pattern(".*")
    // Verify they all have pattern set
    assert(List(t1, t2, t3, t4, t5, t6).forall(_.attrs.pattern.isDefined))

  test("type safety - fields with HasMinMax can use min/max"):
    // These should all compile
    val t1 = "x".i18n.num.min("0").max("10")
    val t2 = "x".i18n.date.min("2020-01-01").max("2030-12-31")
    val t3 = "x".i18n.time.min("09:00").max("17:00")
    val t4 = "x".i18n.range.min("0").max("100")
    // Verify they all have min and max set
    assert(
      List(t1, t2, t3, t4).forall(f =>
        f.attrs.min.isDefined && f.attrs.max.isDefined
      )
    )

  test("type safety - fields with HasPlaceholder can use placeholder"):
    // These should all compile (Text, Email, Tel, Url, Search, Password, Number, Area)
    // Note: File does NOT have HasPlaceholder
    val t1 = "x".i18n.txt.placeholder("...")
    val t2 = "x".i18n.email.placeholder("...")
    val t3 = "x".i18n.tel.placeholder("...")
    val t4 = "x".i18n.url.placeholder("...")
    val t5 = "x".i18n.search.placeholder("...")
    val t6 = "x".i18n.pwd.placeholder("...")
    val t7 = "x".i18n.num.placeholder("...")
    val t8 = "x".i18n.area.placeholder("...")
    // Verify they all have placeholder set
    assert(
      List(t1, t2, t3, t4, t5, t6, t7, t8).forall(_.attrs.placeholder.isDefined)
    )

  // ============================================================
  // Compile-time type safety — compileErrors tests
  // ============================================================

  test("Color cannot use .required (no CanBeRequired)"):
    assert(compileErrors(""""X".i18n.color.required""").nonEmpty)

  test("Hidden cannot use .required (no CanBeRequired)"):
    assert(
      compileErrors("""Field("X".i18n, FieldType.Hidden).required""").nonEmpty
    )

  test("Range cannot use .required (no CanBeRequired)"):
    assert(compileErrors(""""X".i18n.range.required""").nonEmpty)

  test("Number cannot use .pattern (no HasPattern)"):
    assert(compileErrors(""""X".i18n.num.pattern(".*")""").nonEmpty)

  test("Date cannot use .pattern (no HasPattern)"):
    assert(compileErrors(""""X".i18n.date.pattern(".*")""").nonEmpty)

  test("Area cannot use .pattern (no HasPattern)"):
    assert(compileErrors(""""X".i18n.area.pattern(".*")""").nonEmpty)

  test("Text cannot use .min (no HasMinMax)"):
    assert(compileErrors(""""X".i18n.txt.min("0")""").nonEmpty)

  test("Email cannot use .max (no HasMinMax)"):
    assert(compileErrors(""""X".i18n.email.max("10")""").nonEmpty)

  test("Date cannot use .placeholder (no HasPlaceholder)"):
    assert(compileErrors(""""X".i18n.date.placeholder("...")""").nonEmpty)

  test("Range cannot use .placeholder (no HasPlaceholder)"):
    assert(compileErrors(""""X".i18n.range.placeholder("...")""").nonEmpty)

  test("File cannot use .placeholder (no HasPlaceholder)"):
    assert(compileErrors(""""X".i18n.file.placeholder("...")""").nonEmpty)

  // ============================================================
  // Form-level attributes (T006)
  // ============================================================

  test("FormConfig defaults are all empty"):
    val cfg = FormConfig()
    assertEquals(cfg.action, None)
    assertEquals(cfg.method, None)
    assertEquals(cfg.enctype, None)
    assertEquals(cfg.novalidate, false)

  test("Form.Init carries default config"):
    val f = Form("test")
    assertEquals(f.config, FormConfig())

  test(".action sets action on Init"):
    val f = Form("test").action("/submit")
    assertEquals(f.config.action, Some("/submit"))

  test(".method sets method on Init"):
    val f = Form("test").method("post")
    assertEquals(f.config.method, Some("post"))

  test(".enctype sets enctype on Init"):
    val f = Form("test").enctype("multipart/form-data")
    assertEquals(f.config.enctype, Some("multipart/form-data"))

  test(".novalidate sets novalidate on Init"):
    val f = Form("test").novalidate
    assertEquals(f.config.novalidate, true)

  test("chaining multiple config methods"):
    val f = Form("test")
      .action("/api/submit")
      .method("post")
      .enctype("multipart/form-data")
      .novalidate
    assertEquals(f.config.action, Some("/api/submit"))
    assertEquals(f.config.method, Some("post"))
    assertEquals(f.config.enctype, Some("multipart/form-data"))
    assertEquals(f.config.novalidate, true)

  test("config propagates through Init -> Fields"):
    val f = Form("t").action("/go").method("post") |> "Name".i18n.txt
    assertEquals(f.config.action, Some("/go"))
    assertEquals(f.config.method, Some("post"))

  test("config propagates through Fields -> Fields"):
    val f = Form("t").action("/go").|>("A".i18n.txt).|>("B".i18n.email)
    assertEquals(f.config.action, Some("/go"))

  test("config propagates through Init -> InFieldset"):
    val f = Form("t").method("post") |>> "Info".i18n
    assertEquals(f.config.method, Some("post"))

  test("config propagates through InFieldset -> Fields (|<<)"):
    val f = (Form("t").method("post") |>> "Info".i18n |> "A".i18n.txt).|<<
    assertEquals(f.config.method, Some("post"))

  test("config propagates through Fields -> Done"):
    val f =
      Form("t").action("/submit").method("post").|>("A".i18n.txt).|!("Go".i18n)
    f match
      case d: Form.Done =>
        assertEquals(d.config.action, Some("/submit"))
        assertEquals(d.config.method, Some("post"))
      case _ => fail("Expected Done")

  // ============================================================
  // Form rendering with form-level attributes (T006)
  // ============================================================

  test("rendered form has for attribute on labels"):
    val f = Form("user").|>("Email".i18n.email).|!("Send".i18n)
    val node = FormInterpreter.toNode(f)
    val rendered = tags.render.Html.render(node)
    assert(
      rendered.contains("""for="user-email""""),
      s"Missing for attr in: $rendered"
    )

  test("rendered form includes action attribute"):
    val f =
      Form("user").action("/api/submit").|>("Name".i18n.txt).|!("Send".i18n)
    val node = FormInterpreter.toNode(f)
    val rendered = tags.render.Html.render(node)
    assert(
      rendered.contains("""action="/api/submit""""),
      s"Missing action in: $rendered"
    )

  test("rendered form includes method attribute"):
    val f = Form("user").method("post").|>("Name".i18n.txt).|!("Send".i18n)
    val node = FormInterpreter.toNode(f)
    val rendered = tags.render.Html.render(node)
    assert(
      rendered.contains("""method="post""""),
      s"Missing method in: $rendered"
    )

  test("rendered form includes enctype attribute"):
    val f = Form("upload")
      .enctype("multipart/form-data")
      .|>("File".i18n.file)
      .|!("Upload".i18n)
    val node = FormInterpreter.toNode(f)
    val rendered = tags.render.Html.render(node)
    assert(
      rendered.contains("""enctype="multipart/form-data""""),
      s"Missing enctype in: $rendered"
    )

  test("rendered form includes novalidate attribute"):
    val f = Form("quick").novalidate.|>("Name".i18n.txt).|!("Go".i18n)
    val node = FormInterpreter.toNode(f)
    val rendered = tags.render.Html.render(node)
    assert(rendered.contains("novalidate"), s"Missing novalidate in: $rendered")

  test("rendered form with all config attributes"):
    val f = Form("contact")
      .action("/api/contact")
      .method("post")
      .enctype("multipart/form-data")
      .novalidate
      .|>("Name".i18n.txt.required)
      .|>("Email".i18n.email)
      .|!("Submit".i18n)
    val node = FormInterpreter.toNode(f)
    val rendered = tags.render.Html.render(node)
    assert(
      rendered.contains("""id="contact-form""""),
      s"Missing id in: $rendered"
    )
    assert(
      rendered.contains("""action="/api/contact""""),
      s"Missing action in: $rendered"
    )
    assert(
      rendered.contains("""method="post""""),
      s"Missing method in: $rendered"
    )
    assert(
      rendered.contains("""enctype="multipart/form-data""""),
      s"Missing enctype in: $rendered"
    )
    assert(rendered.contains("novalidate"), s"Missing novalidate in: $rendered")

  test("rendered form without config has only id"):
    val f = Form("simple").|>("Name".i18n.txt).|!("OK".i18n)
    val node = FormInterpreter.toNode(f)
    val rendered = tags.render.Html.render(node)
    assert(
      rendered.startsWith("""<form id="simple-form">"""),
      s"Unexpected form tag: $rendered"
    )
    assert(!rendered.contains("action="), s"Unexpected action in: $rendered")
    assert(!rendered.contains("method="), s"Unexpected method in: $rendered")

  // ============================================================
  // Radio button groups
  // ============================================================

  test("RadioBuilder chains options"):
    val rb = RadioBuilder("Q".i18n, Vector("A".i18n))
    val rb2 = rb ? "B".i18n ? "C".i18n
    assertEquals(rb2.options.size, 3)

  test("FormEntry.Radio wraps RadioGroup"):
    val rg = RadioGroup("Q".i18n, Vector("A".i18n, "B".i18n))
    val entry = FormEntry.Radio(rg)
    entry match
      case FormEntry.Radio(g) => assertEquals(g.label, rg.label)
      case _                  => fail("wrong entry type")

  test("I18n ?? I18n creates RadioBuilder"):
    val rb = "Satisfaction".i18n ?? "Good".i18n
    assertEquals(rb.label.key, "Satisfaction")
    assertEquals(rb.options.size, 1)

  test("RadioBuilder ? chains options"):
    val rb = "Rating".i18n ?? "1".i18n ? "2".i18n ? "3".i18n
    assertEquals(rb.options.size, 3)

  test("Init |@ creates radio entry"):
    val form = Form("test") |@ ("Q".i18n ?? "A".i18n ? "B".i18n)
    form match
      case Form.Fields(_, _, fields) =>
        assertEquals(fields.size, 1)
        fields.head match
          case FormEntry.Radio(rg) => assertEquals(rg.options.size, 2)
          case _                   => fail("expected Radio entry")
      case _ => fail("expected Fields state")

  test("Fields |@ appends radio entry"):
    val form = Form("test")
      .|>("Name".i18n.txt)
      .|@("Agree".i18n ?? "Yes".i18n ? "No".i18n)
    form match
      case Form.Fields(_, _, fields) =>
        assertEquals(fields.size, 2)
      case _ => fail("expected Fields state")

  test("InFieldset |@ adds radio to inner"):
    val form = Form("test")
      .|>>("Section".i18n)
      .|@("Q".i18n ?? "A".i18n ? "B".i18n)
      .|<<
    form match
      case Form.Fields(_, _, fields) =>
        assertEquals(fields.size, 1)
        fields.head match
          case FormEntry.Fieldset(_, inner) =>
            assertEquals(inner.size, 1)
            inner.head match
              case FormEntry.Radio(rg) => assertEquals(rg.options.size, 2)
              case _                   => fail("expected Radio in fieldset")
          case _ => fail("expected Fieldset")
      case _ => fail("expected Fields state")

  test("multiple radio groups"):
    val form = Form("test")
      .|@("Q1".i18n ?? "A".i18n ? "B".i18n)
      .|@("Q2".i18n ?? "X".i18n ? "Y".i18n)
      .|!("Submit".i18n)
    form match
      case d: Form.Done =>
        assertEquals(d.fields.size, 2)
        assert(d.fields(0).isInstanceOf[FormEntry.Radio])
        assert(d.fields(1).isInstanceOf[FormEntry.Radio])
      case _ => fail("expected Done")

  test("radio in fieldset"):
    val form = Form("test")
      .|>>("Section".i18n)
      .|@("Q".i18n ?? "A".i18n ? "B".i18n)
      .|<<
      .|!("Submit".i18n)
    form match
      case d: Form.Done =>
        assertEquals(d.fields.size, 1)
        d.fields.head match
          case FormEntry.Fieldset(_, inner) =>
            assertEquals(inner.size, 1)
            assert(inner.head.isInstanceOf[FormEntry.Radio])
          case _ => fail("expected Fieldset")
      case _ => fail("expected Done")

  test("form with only a radio group"):
    val form = Form("test")
      .|@("Q".i18n ?? "A".i18n ? "B".i18n)
      .|!("Submit".i18n)
    form match
      case d: Form.Done =>
        assertEquals(d.fields.size, 1)
        assert(d.fields.head.isInstanceOf[FormEntry.Radio])
      case _ => fail("expected Done")

  test("rendered radio has shared name attribute"):
    val form = Form("test")
      .|@("Color".i18n ?? "Red".i18n ? "Blue".i18n ? "Green".i18n)
      .|!("Submit".i18n)
    val html = tags.render.Html.render(FormInterpreter.toNode(form))
    // All radios should have name="test-color"
    val count = html.split("""name="test-color"""").length - 1
    assertEquals(
      count,
      3,
      s"Expected 3 occurrences of name=test-color, found $count in: $html"
    )

  test("rendered radio contains type=radio"):
    val form = Form("survey")
      .|>("Name".i18n.txt)
      .|@("Satisfaction".i18n ?? "Good".i18n ? "OK".i18n ? "Bad".i18n)
      .|!("Submit".i18n)
    val html = tags.render.Html.render(FormInterpreter.toNode(form))
    assert(html.contains("""type="radio""""), s"Missing type=radio in: $html")
    assert(html.contains("Satisfaction"), s"Missing Satisfaction in: $html")

  test("rendered radio has correct structure"):
    val form = Form("form")
      .|@("Color".i18n ?? "Red".i18n ? "Blue".i18n)
      .|!("Submit".i18n)
    val html = tags.render.Html.render(FormInterpreter.toNode(form))
    assert(html.contains("<fieldset>"), s"Missing fieldset in: $html")
    assert(html.contains("<legend>Color</legend>"), s"Missing legend in: $html")
    assert(
      html.contains("""id="form-color-red""""),
      s"Missing id form-color-red in: $html"
    )
    assert(
      html.contains("""id="form-color-blue""""),
      s"Missing id form-color-blue in: $html"
    )
