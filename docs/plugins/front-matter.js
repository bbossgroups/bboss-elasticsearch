/*!
 * Docsify Plugin: front-matter v5.0.0
 * https://docsify.js.org
 * (c) 2017-2026
 * MIT license
 */
(function() {
    "use strict";
    var errors = [], reference_blocks = [], processing_time = 0, regex$1 = {
        regLevel: new RegExp("^([\\s\\-]+)"),
        invalidLine: new RegExp("^\\-\\-\\-|^\\.\\.\\.|^\\s*#.*|^\\s*$"),
        dashesString: new RegExp('^\\s*\\"([^\\"]*)\\"\\s*$'),
        quotesString: new RegExp("^\\s*\\'([^\\']*)\\'\\s*$"),
        float: new RegExp("^[+-]?[0-9]+\\.[0-9]+(e[+-]?[0-9]+(\\.[0-9]+)?)?$"),
        integer: new RegExp("^[+-]?[0-9]+$"),
        array: new RegExp("\\[\\s*(.*)\\s*\\]"),
        map: new RegExp("\\{\\s*(.*)\\s*\\}"),
        key_value: new RegExp("([a-z0-9_-][ a-z0-9_-]*):( .+)", "i"),
        single_key_value: new RegExp("^([a-z0-9_-][ a-z0-9_-]*):( .+?)$", "i"),
        key: new RegExp("([a-z0-9_-][ a-z0-9_-]+):( .+)?", "i"),
        item: new RegExp("^-\\s+"),
        trim: new RegExp("^\\s+|\\s+$"),
        comment: new RegExp("([^\\'\\\"#]+([\\'\\\"][^\\'\\\"]*[\\'\\\"])*)*(#.*)?")
    };
    function Block(lvl) {
        return {
            parent: null,
            length: 0,
            level: lvl,
            lines: [],
            children: [],
            addChild: function(obj) {
                this.children.push(obj);
                obj.parent = this;
                ++this.length;
            }
        };
    }
    function parser(str) {
        var regLevel = regex$1["regLevel"];
        var invalidLine = regex$1["invalidLine"];
        var lines = str.split("\n");
        var m;
        var level = 0, curLevel = 0;
        var blocks = [];
        var result = new Block(-1);
        var currentBlock = new Block(0);
        result.addChild(currentBlock);
        var levels = [];
        var line = "";
        blocks.push(currentBlock);
        levels.push(level);
        for (var i = 0, len = lines.length; i < len; ++i) {
            line = lines[i];
            if (line.match(invalidLine)) {
                continue;
            }
            if (m = regLevel.exec(line)) {
                level = m[1].length;
            } else level = 0;
            if (level > curLevel) {
                var oldBlock = currentBlock;
                currentBlock = new Block(level);
                oldBlock.addChild(currentBlock);
                blocks.push(currentBlock);
                levels.push(level);
            } else if (level < curLevel) {
                var added = false;
                var k = levels.length - 1;
                for (;k >= 0; --k) {
                    if (levels[k] == level) {
                        currentBlock = new Block(level);
                        blocks.push(currentBlock);
                        levels.push(level);
                        if (blocks[k].parent != null) blocks[k].parent.addChild(currentBlock);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    errors.push("Error: Invalid indentation at line " + i + ": " + line);
                    return;
                }
            }
            currentBlock.lines.push(line.replace(regex$1["trim"], ""));
            curLevel = level;
        }
        return result;
    }
    function processValue(val) {
        val = val.replace(regex$1["trim"], "");
        var m = null;
        if (val == "true") {
            return true;
        } else if (val == "false") {
            return false;
        } else if (val == ".NaN") {
            return Number.NaN;
        } else if (val == "null") {
            return null;
        } else if (val == ".inf") {
            return Number.POSITIVE_INFINITY;
        } else if (val == "-.inf") {
            return Number.NEGATIVE_INFINITY;
        } else if (m = val.match(regex$1["dashesString"])) {
            return m[1];
        } else if (m = val.match(regex$1["quotesString"])) {
            return m[1];
        } else if (m = val.match(regex$1["float"])) {
            return parseFloat(m[0]);
        } else if (m = val.match(regex$1["integer"])) {
            return parseInt(m[0]);
        } else if (!isNaN(m = Date.parse(val))) {
            return new Date(m);
        } else if (m = val.match(regex$1["single_key_value"])) {
            var res = {};
            res[m[1]] = processValue(m[2]);
            return res;
        } else if (m = val.match(regex$1["array"])) {
            var count = 0, c = " ";
            var res = [];
            var content = "";
            var str = false;
            for (var j = 0, lenJ = m[1].length; j < lenJ; ++j) {
                c = m[1][j];
                if (c == "'" || c == '"') {
                    if (str === false) {
                        str = c;
                        content += c;
                        continue;
                    } else if (c == "'" && str == "'" || c == '"' && str == '"') {
                        str = false;
                        content += c;
                        continue;
                    }
                } else if (str === false && (c == "[" || c == "{")) {
                    ++count;
                } else if (str === false && (c == "]" || c == "}")) {
                    --count;
                } else if (str === false && count == 0 && c == ",") {
                    res.push(processValue(content));
                    content = "";
                    continue;
                }
                content += c;
            }
            if (content.length > 0) res.push(processValue(content));
            return res;
        } else if (m = val.match(regex$1["map"])) {
            var count = 0, c = " ";
            var res = [];
            var content = "";
            var str = false;
            for (var j = 0, lenJ = m[1].length; j < lenJ; ++j) {
                c = m[1][j];
                if (c == "'" || c == '"') {
                    if (str === false) {
                        str = c;
                        content += c;
                        continue;
                    } else if (c == "'" && str == "'" || c == '"' && str == '"') {
                        str = false;
                        content += c;
                        continue;
                    }
                } else if (str === false && (c == "[" || c == "{")) {
                    ++count;
                } else if (str === false && (c == "]" || c == "}")) {
                    --count;
                } else if (str === false && count == 0 && c == ",") {
                    res.push(content);
                    content = "";
                    continue;
                }
                content += c;
            }
            if (content.length > 0) res.push(content);
            var newRes = {};
            for (var j = 0, lenJ = res.length; j < lenJ; ++j) {
                if (m = res[j].match(regex$1["key_value"])) {
                    newRes[m[1]] = processValue(m[2]);
                }
            }
            return newRes;
        } else return val;
    }
    function processFoldedBlock(block) {
        var lines = block.lines;
        var children = block.children;
        var str = lines.join(" ");
        var chunks = [ str ];
        for (var i = 0, len = children.length; i < len; ++i) {
            chunks.push(processFoldedBlock(children[i]));
        }
        return chunks.join("\n");
    }
    function processLiteralBlock(block) {
        var lines = block.lines;
        var children = block.children;
        var str = lines.join("\n");
        for (var i = 0, len = children.length; i < len; ++i) {
            str += processLiteralBlock(children[i]);
        }
        return str;
    }
    function processBlock(blocks) {
        var m = null;
        var res = {};
        var lines = null;
        var children = null;
        var currentObj = null;
        var level = -1;
        var processedBlocks = [];
        var isMap = true;
        for (var j = 0, lenJ = blocks.length; j < lenJ; ++j) {
            if (level != -1 && level != blocks[j].level) continue;
            processedBlocks.push(j);
            level = blocks[j].level;
            lines = blocks[j].lines;
            children = blocks[j].children;
            currentObj = null;
            for (var i = 0, len = lines.length; i < len; ++i) {
                var line = lines[i];
                if (m = line.match(regex$1["key"])) {
                    var key = m[1];
                    if (key[0] == "-") {
                        key = key.replace(regex$1["item"], "");
                        if (isMap) {
                            isMap = false;
                            if (typeof res.length === "undefined") {
                                res = [];
                            }
                        }
                        if (currentObj != null) res.push(currentObj);
                        currentObj = {};
                        isMap = true;
                    }
                    if (typeof m[2] != "undefined") {
                        var value = m[2].replace(regex$1["trim"], "");
                        if (value[0] == "&") {
                            var nb = processBlock(children);
                            if (currentObj != null) currentObj[key] = nb; else res[key] = nb;
                            reference_blocks[value.substr(1)] = nb;
                        } else if (value[0] == "|") {
                            if (currentObj != null) currentObj[key] = processLiteralBlock(children.shift()); else res[key] = processLiteralBlock(children.shift());
                        } else if (value[0] == "*") {
                            var v = value.substr(1);
                            var no = {};
                            if (typeof reference_blocks[v] == "undefined") {
                                errors.push("Reference '" + v + "' not found!");
                            } else {
                                for (var k in reference_blocks[v]) {
                                    no[k] = reference_blocks[v][k];
                                }
                                if (currentObj != null) currentObj[key] = no; else res[key] = no;
                            }
                        } else if (value[0] == ">") {
                            if (currentObj != null) currentObj[key] = processFoldedBlock(children.shift()); else res[key] = processFoldedBlock(children.shift());
                        } else {
                            if (currentObj != null) currentObj[key] = processValue(value); else res[key] = processValue(value);
                        }
                    } else {
                        if (currentObj != null) currentObj[key] = processBlock(children); else res[key] = processBlock(children);
                    }
                } else if (line.match(/^-\s*$/)) {
                    if (isMap) {
                        isMap = false;
                        if (typeof res.length === "undefined") {
                            res = [];
                        }
                    }
                    if (currentObj != null) res.push(currentObj);
                    currentObj = {};
                    isMap = true;
                    continue;
                } else if (m = line.match(/^-\s*(.*)/)) {
                    if (currentObj != null) currentObj.push(processValue(m[1])); else {
                        if (isMap) {
                            isMap = false;
                            if (typeof res.length === "undefined") {
                                res = [];
                            }
                        }
                        res.push(processValue(m[1]));
                    }
                    continue;
                }
            }
            if (currentObj != null) {
                if (isMap) {
                    isMap = false;
                    if (typeof res.length === "undefined") {
                        res = [];
                    }
                }
                res.push(currentObj);
            }
        }
        for (var j = processedBlocks.length - 1; j >= 0; --j) {
            blocks.splice.call(blocks, processedBlocks[j], 1);
        }
        return res;
    }
    function semanticAnalysis(blocks) {
        var res = processBlock(blocks.children);
        return res;
    }
    function preProcess(src) {
        var m;
        var lines = src.split("\n");
        var r = regex$1["comment"];
        for (var i in lines) {
            if (m = lines[i].match(r)) {
                if (typeof m[3] !== "undefined") {
                    lines[i] = m[0].substr(0, m[0].length - m[3].length);
                }
            }
        }
        return lines.join("\n");
    }
    function load(str) {
        errors = [];
        reference_blocks = [];
        processing_time = (new Date).getTime();
        var pre = preProcess(str);
        var doc = parser(pre);
        var res = semanticAnalysis(doc);
        processing_time = (new Date).getTime() - processing_time;
        return res;
    }
    const optionalByteOrderMark = "\\ufeff?";
    const pattern = "^(" + optionalByteOrderMark + "(= yaml =|---)" + "$([\\s\\S]*?)" + "(?:\\2|\\.\\.\\.)" + "$" + "" + "(?:\\n)?)";
    const regex = new RegExp(pattern, "m");
    function extractor(string) {
        string = string || "";
        const lines = string.split(/(\r?\n)/);
        if (lines[0] && /= yaml =|---/.test(lines[0])) {
            return parse(string);
        } else {
            return {
                attributes: {},
                body: string
            };
        }
    }
    function parse(string) {
        const match = regex.exec(string);
        if (!match) {
            return {
                attributes: {},
                body: string
            };
        }
        const yaml = match[match.length - 1].replace(/^\s+|\s+$/g, "");
        const attributes = load(yaml) || {};
        const body = string.replace(match[0], "");
        return {
            attributes: attributes,
            body: body,
            frontmatter: yaml
        };
    }
    const install = function(hook, vm) {
        vm.config.frontMatter = {};
        vm.config.frontMatter.installed = true;
        vm.config.frontMatter.parseMarkdown = function(content) {
            const {body: body} = extractor(content);
            return body;
        };
        hook.beforeEach((content => {
            const {attributes: attributes, body: body} = extractor(content);
            vm.frontmatter = attributes;
            return body;
        }));
    };
    window.$docsify = window.$docsify || {};
    window.$docsify.plugins = [ install, ...window.$docsify?.plugins || [] ];
})();
