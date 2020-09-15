package org.frameworkset.elasticsearch.serial;

import com.fasterxml.jackson.core.io.CharTypes;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public class CustomCharEscapeUtil  extends CharEscapeUtil
{
	private final static int[] _customOutputEscapes;
	static {
		int temp[] = new int[128];
		int[] sOutputEscapes = CharTypes.get7BitOutputEscapes();
		for(int i = 0 ; i < sOutputEscapes.length; i ++) {
			temp[i] = sOutputEscapes[i];

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
		_customOutputEscapes = temp;
	}
	private boolean esEncode;
	public CustomCharEscapeUtil(Writer w, int features,boolean esEncode) {
		super(w, features);
		this.esEncode = esEncode;
	}

	public CustomCharEscapeUtil(boolean esEncode) {
		super();

		this.esEncode = esEncode;
	}


	public CustomCharEscapeUtil(Writer w,boolean esEncode) {
		super(w);

		this.esEncode = esEncode;
	}

	@Override
	public void _writeString(String text) throws IOException {
		if(esEncode)
			text = escape(text);
		super._writeString(text);
	}
}
