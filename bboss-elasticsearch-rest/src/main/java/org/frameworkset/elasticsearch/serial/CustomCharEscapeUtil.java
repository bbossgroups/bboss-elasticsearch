package org.frameworkset.elasticsearch.serial;

import java.io.Writer;

/**
 *
 */
public class CustomCharEscapeUtil  extends CharEscapeUtil
{

	public CustomCharEscapeUtil(Writer w, int features) {
		super(w, features);
		extendEscapse();
	}

	public CustomCharEscapeUtil() {
		super();
		extendEscapse();
	}
	private void extendEscapse(){
		int temp[] = new int[128];
		for(int i = 0 ; i < _outputEscapes.length; i ++) {
			temp[i] = _outputEscapes[i];

		}
		//		escapeCodesForAscii['\\'] = '\\';
		temp['+'] = '+';
		temp['-'] = '-';
		temp['!'] = '!';

		temp['('] = '(';

		temp[')'] = ')';

		temp[':'] = ':';

		temp['^'] = '^';

		temp['['] = '[';
		temp[']'] = ']';
//		escapeCodesForAscii['\"'] = '\"';
		temp['{'] = '{';
		temp['}'] = '}';
		temp['~'] = '~';
		temp['*'] = '*';
		temp['?'] = '?';
		temp['|'] = '|';

		temp['&'] = '&';

		temp['/'] = '/';
		this._outputEscapes = temp;
	}

	public CustomCharEscapeUtil(Writer w) {
		super(w);
		extendEscapse();
	}
}
