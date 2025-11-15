# -----------------------------
# Project config (override as needed)
# -----------------------------
MILL             ?= mill
SCALA_VERSION    ?= 3.7.3
WATCH            ?= -w          # set to empty to disable mill watch mode

# Example module coordinates
EXAMPLE     := example
EXAMPLE_JVM := $(EXAMPLE).jvm["$(SCALA_VERSION)"]
EXAMPLE_JS	:= $(EXAMPLE).js["$(SCALA_VERSION)"]

# JS output folders produced by mill
JS_OUT_FAST      := out/example/js/$(SCALA_VERSION)/fastLinkJS.dest
JS_OUT_FULL      := out/example/js/$(SCALA_VERSION)/fullLinkJS.dest

# Where we symlink the generated JS sources for Vite to pick up
WEB_LINK_TARGET  := example/web/src/scala

# -----------------------------
# Phony
# -----------------------------
.PHONY: help bsp idea ide-setup example-watch-html js-example-watch-compile js-fast js-full \
        link-fast link-full web-install web-dev web-clean clean show-config

# -----------------------------
# Help
# -----------------------------
help:
	@echo ""
	@echo "Targets:"
	@echo "  show-config                - Print resolved settings (mill binary, Scala version, watch flag)."
	@echo "  bsp                        - Install Mill BSP (IDE integration)."
	@echo "  idea                       - Generate IntelliJ project files (if supported by your mill)."
	@echo "  ide-setup                  - Do both: bsp + idea."
	@echo ""
	@echo "Example app / Scala.js:"
	@echo "  example-watch-html         - Run JVM side to generate HTML (writes example/web/src/html/index.html)."
	@echo "  js-example-watch-compile   - Watch-compile Scala.js sources (no JS bundling)."
	@echo "  js-watch-example-fastlink  - Watch fastLinkJS (dev JS bundle, rebuild on changes)."
	@echo "  js-example-fastlink        - One-off fastLinkJS (dev mode JS bundle)."
	@echo "  js-example-full            - One-off fullLinkJS (optimized prod JS bundle)."
	@echo "  link-fast                  - Symlink fastLinkJS.dest into $(WEB_LINK_TARGET)."
	@echo "  link-full                  - Symlink fullLinkJS.dest into $(WEB_LINK_TARGET)."
	@echo ""
	@echo "Web (Vite):"
	@echo "  web-install                - npm install (in example/web)."
	@echo "  web-dev                    - Run Vite dev server (in example/web)."
	@echo ""
	@echo "Housekeeping:"
	@echo "  web-clean                  - Remove the symlinked $(WEB_LINK_TARGET)."
	@echo "  clean                      - Remove mill out/ and link target."
	@echo ""
	@echo "Usage examples:"
	@echo "  make bsp"
	@echo "  make example-watch-html"
	@echo "  make js-example-fastlink link-fast web-install web-dev"
	@echo "  make SCALA_VERSION=3.7.3 js-example-full link-full"
	@echo ""

show-config:
	@echo "MILL           = $(MILL)"
	@echo "SCALA_VERSION  = $(SCALA_VERSION)"
	@echo "WATCH          = $(WATCH)"
	@echo "EXAMPLE   = $(EXAMPLE)"
	@echo "JS_OUT_FAST    = $(JS_OUT_FAST)"
	@echo "JS_OUT_FULL    = $(JS_OUT_FULL)"
	@echo "WEB_LINK_TARGET= $(WEB_LINK_TARGET)"

# -----------------------------
# IDE
# -----------------------------
bsp:
	$(MILL) --bsp-install

idea:
	$(MILL) mill.idea

ide-setup: bsp idea

# -----------------------------
# Example app / Scala.js
# -----------------------------
# Generate HTML by running the JVM example (as per README)
example-watch-html:
	$(MILL) $(WATCH) $(EXAMPLE_JVM).run

# Just compile the Scala.js (no linking)
js-example-watch-compile:
	$(MILL) $(WATCH) $(EXAMPLE_JS).compile

# Dev JS bundle
js-watch-example-fastlink:
	$(MILL) $(WATCH) $(EXAMPLE_JS).fastLinkJS

# Dev JS bundle
js-example-fastlink:
	$(MILL) $(EXAMPLE_JS).fastLinkJS

# Optimized prod JS bundle
js-example-full:
	$(MILL) $(EXAMPLE_JS).fullLinkJS

# -----------------------------
# Symlinks into web app
# -----------------------------
# Symlink fastLinkJS.dest -> example/web/src/scala
link-fast: js-example-fastlink
	@mkdir -p example/web/src
	@test ! -e "$(WEB_LINK_TARGET)" || rm -rf "$(WEB_LINK_TARGET)"
	ln -sfn "$$(realpath "$(JS_OUT_FAST)")" "$(WEB_LINK_TARGET)"
	@echo "Linked: $(WEB_LINK_TARGET) -> $$(realpath "$(JS_OUT_FAST)")"

# Symlink fullLinkJS.dest -> example/web/src/scala
link-full: js-example-full
	@mkdir -p example/web/src/scala
	@test ! -e "$(WEB_LINK_TARGET)" || rm -rf "$(WEB_LINK_TARGET)"
	ln -sfn "$$(realpath "$(JS_OUT_FULL)")" "$(WEB_LINK_TARGET)"
	@echo "Linked: $(WEB_LINK_TARGET) -> $$(realpath "$(JS_OUT_FULL)")"

# -----------------------------
# Web (Vite)
# -----------------------------
web-install:
	cd example/web && npm install

web-dev:
	cd example/web && npm run dev

# -----------------------------
# Cleaning
# -----------------------------
web-clean:
	@test ! -e "$(WEB_LINK_TARGET)" || rm -rf "$(WEB_LINK_TARGET)"
	@echo "Removed link/dir: $(WEB_LINK_TARGET)"

clean: web-clean
	@test ! -e out || rm -rf out
	@echo "Removed: out/"

