package htmx

import tags.Attr

/** HTMX Attribute Extensions
  *
  * Provides type-safe extensions for HTMX attributes. Import with:
  * {{{
  * import htmx.hx.*
  * }}}
  *
  * ==Request Methods==
  * {{{
  * button | "/api/data".hxGet
  * form | "/api/submit".hxPost
  * }}}
  *
  * ==Targeting==
  * {{{
  * button | "#result".hxTarget | "innerHTML".hxSwap
  * }}}
  *
  * ==Triggers==
  * {{{
  * input | "keyup changed delay:500ms".hxTrigger
  * }}}
  *
  * ==Event Handlers==
  * {{{
  * button | "console.log('clicked')".hxOn("click")
  * div | "showSpinner()".hxOn("htmx:beforeRequest")
  * }}}
  *
  * @see
  *   https://htmx.org/reference/
  */
object hx:

  extension (s: String)

    // ============================================================
    // Request Methods (TICKET-HX-001)
    // ============================================================

    /** Create `hx-get` attribute for GET requests */
    def hxGet: Attr = Attr.Custom("hx-get", s)

    /** Create `hx-post` attribute for POST requests */
    def hxPost: Attr = Attr.Custom("hx-post", s)

    /** Create `hx-put` attribute for PUT requests */
    def hxPut: Attr = Attr.Custom("hx-put", s)

    /** Create `hx-patch` attribute for PATCH requests */
    def hxPatch: Attr = Attr.Custom("hx-patch", s)

    /** Create `hx-delete` attribute for DELETE requests */
    def hxDelete: Attr = Attr.Custom("hx-delete", s)

    // ============================================================
    // Targeting (TICKET-HX-002)
    // ============================================================

    /** Create `hx-target` attribute - CSS selector for response target */
    def hxTarget: Attr = Attr.Custom("hx-target", s)

    /** Create `hx-swap` attribute - how to swap content (innerHTML, outerHTML,
      * etc)
      */
    def hxSwap: Attr = Attr.Custom("hx-swap", s)

    /** Create `hx-select` attribute - CSS selector to pick from response */
    def hxSelect: Attr = Attr.Custom("hx-select", s)

    /** Create `hx-select-oob` attribute - out-of-band content selector */
    def hxSelectOob: Attr = Attr.Custom("hx-select-oob", s)

    // ============================================================
    // Triggers and Confirmation (TICKET-HX-003)
    // ============================================================

    /** Create `hx-trigger` attribute - event that triggers request */
    def hxTrigger: Attr = Attr.Custom("hx-trigger", s)

    /** Create `hx-confirm` attribute - confirmation message before request */
    def hxConfirm: Attr = Attr.Custom("hx-confirm", s)

    /** Create `hx-prompt` attribute - prompt for user input before request */
    def hxPrompt: Attr = Attr.Custom("hx-prompt", s)

    // ============================================================
    // Inclusion (TICKET-HX-004)
    // ============================================================

    /** Create `hx-include` attribute - CSS selector for additional inputs */
    def hxInclude: Attr = Attr.Custom("hx-include", s)

    /** Create `hx-vals` attribute - JSON values to include in request */
    def hxVals: Attr = Attr.Custom("hx-vals", s)

    /** Create `hx-headers` attribute - JSON headers to include */
    def hxHeaders: Attr = Attr.Custom("hx-headers", s)

    // ============================================================
    // Loading Indicators (TICKET-HX-005)
    // ============================================================

    /** Create `hx-indicator` attribute - element to show during request */
    def hxIndicator: Attr = Attr.Custom("hx-indicator", s)

    /** Create `hx-disabled-elt` attribute - element(s) to disable during
      * request
      */
    def hxDisabledElt: Attr = Attr.Custom("hx-disabled-elt", s)

    // ============================================================
    // Behavior (TICKET-HX-006)
    // ============================================================

    /** Create `hx-push-url` attribute - push URL to history */
    def hxPushUrl: Attr = Attr.Custom("hx-push-url", s)

    /** Create `hx-sync` attribute - synchronization strategy */
    def hxSync: Attr = Attr.Custom("hx-sync", s)

    // ============================================================
    // Event Handlers (TICKET-HX-007)
    // ============================================================

    /** Create `hx-on:*` attribute for event handlers
      * @param event
      *   The event name (e.g., "click", "htmx:beforeRequest")
      */
    def hxOn(event: String): Attr = Attr.Custom(s"hx-on:$event", s)

  /** Boolean-style HTMX attributes */
  object HxBool:
    /** Create `hx-boost="true"` attribute - boost all links/forms in element */
    val hxBoost: Attr = Attr.Custom("hx-boost", "true")

    /** Create `hx-preserve` attribute - preserve element across swaps */
    val hxPreserve: Attr = Attr.Custom("hx-preserve", "true")

    /** Create `hx-history="false"` attribute - disable history for element */
    val hxHistoryFalse: Attr = Attr.Custom("hx-history", "false")

    /** Create `hx-history-elt` attribute - mark element for history snapshots
      */
    val hxHistoryElt: Attr = Attr.Custom("hx-history-elt", "true")
