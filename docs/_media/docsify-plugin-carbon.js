(function() {
        !function(n) {
            n.DocsifyCarbon = {
                create: function(e, a) {
                    return function(t, o) {
                        t.ready(function() {
                            // n.DocsifyCarbon.injectCarbonStyle()
                        }),
                            t.doneEach(function() {
                                // n.DocsifyCarbon.injectCarbonScript(e, a)
                            })
                    }
                },
                injectCarbonScript: function(n, e) {
                    var a = document.querySelector("#carbonads")
                        , t = document.querySelector(".sidebar-nav");
                    if (!a && t) {
                        var o = document.querySelector("#".concat("_carbonads_js"));
                        o ? o = o.parentNode.removeChild(o) : ((o = document.createElement("script")).src = "https://cdn.carbonads.com/carbon.js?serve=".concat(n, "&placement=").concat(e),
                            o.async = "async",
                            o.id = "_carbonads_js"),
                            t.insertBefore(o, t.firstChild)
                    }
                },
                injectCarbonStyle: function() {
                    var n = document.createElement("style");
                    n.textContent = "\n        #carbonads * {\n          margin: initial;\n          padding: initial;\n        }\n\n        #carbonads {\n          max-width: 330px;\n          background-color: #fafafa;\n          font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto,\n            Oxygen-Sans, Ubuntu, Cantarell, \"Helvetica Neue\", Helvetica, Arial,\n            sans-serif;\n        }\n\n        #carbonads a {\n          color: inherit;\n          text-decoration: none;\n        }\n\n        #carbonads a:hover {\n          color: inherit;\n        }\n\n        #carbonads span {\n          display: block;\n          position: relative;\n          overflow: hidden;\n        }\n\n        #carbonads .carbon-wrap {\n          display: flex;\n        }\n\n        #carbonads .carbon-img img {\n          display: block;\n        }\n\n        #carbonads .carbon-text {\n          align-self: center;\n          margin-bottom: 20px;\n          padding: 8px 10px;\n          font-size: 12px;\n          line-height: 1.5;\n          text-align: left;\n        }\n\n        #carbonads .carbon-poweredby {\n          display: block;\n          position: absolute;\n          bottom: 0;\n          right: 0;\n          padding: 6px 8px;\n          border-top-left-radius: 3px;\n          background-color: #f1f1f1;\n          font-size: 8px;\n          font-weight: 600;\n          line-height: 1;\n          letter-spacing: 0.5px;\n          text-align: center;\n          text-transform: uppercase;\n        }\n      ",
                        document.head.insertBefore(n, document.querySelector("head style, head link[rel*='stylesheet']"))
                }
            }
        }(window);
    }
)();
