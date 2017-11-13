package org.frameworkset.elasticsearch.serial;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.soa.BBossStringWriter;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public class CharEscapeUtil  //extends JsonGeneratorImpl
{
	final protected static int SHORT_WRITE = 32;

	final protected static char[] HEX_CHARS = CharTypes.copyHexChars();

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

	final protected Writer _writer;

    /*
    /**********************************************************
    /* Output buffering
    /**********************************************************
     */

	/**
	 * Intermediate buffer in which contents are buffered before
	 * being written using {@link #_writer}.
	 */
	protected char[] _outputBuffer;

	/**
	 * Pointer to the first buffered character to output
	 */
	protected int _outputHead;

	/**
	 * Pointer to the position right beyond the last character to output
	 * (end marker; may point to position right beyond the end of the buffer)
	 */
	protected int _outputTail;

	/**
	 * End marker of the output buffer; one past the last valid position
	 * within the buffer.
	 */
	protected int _outputEnd;

	/**
	 * Short (14 char) temporary buffer allocated if needed, for constructing
	 * escape sequences
	 */
	protected char[] _entityBuffer;

	/**
	 * When custom escapes are used, this member variable is used
	 * internally to hold a reference to currently used escape
	 */
	protected SerializableString _currentEscape;

	/**
	 * Intermediate buffer in which characters of a String are copied
	 * before being encoded.
	 *
	 * @since 2.9
	 */
	protected char[] _charBuffer;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

	public CharEscapeUtil( Writer w,int features)
	{

		_writer = w;
		_outputBuffer = new char[1024];
		_outputEnd = _outputBuffer.length;
		if (JsonGenerator.Feature.ESCAPE_NON_ASCII.enabledIn(features)) {
			// inlined `setHighestNonEscapedChar()`
			_maximumNonEscapedChar = 127;
		}
	}
	
	public CharEscapeUtil( )
	{

		_writer = new BBossStringWriter();
		_outputBuffer = new char[1024];
		_outputEnd = _outputBuffer.length;
		 
	}
	
	public CharEscapeUtil( Writer w)
	{

		_writer = w;
		_outputBuffer = new char[1024];
		_outputEnd = _outputBuffer.length;
		
	}

    /*
    /**********************************************************
    /* Overridden configuration, introspection methods
    /**********************************************************
     */

	
	public Object getOutputTarget() {
		return _writer;
	}


	public int getOutputBuffered() {
		// Assuming tail and head are kept but... trust and verify:
		int len = _outputTail - _outputHead;
		return Math.max(0, len);
	}

	// json does allow this so
	
	public boolean canWriteFormattedNumbers() { return true; }

    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */




    /*
    /**********************************************************
    /* Output method implementations, textual
    /**********************************************************
     */


	public void writeString(String text,boolean flush)
	{
		try {

			_writeString(text);
			if(flush)
				this._flushBuffer();
		}
		catch (IOException e){
			throw new ElasticSearchException(e);
		}

	}
	
	public String toString(){
		try {
			this._flushBuffer();
		} catch (IOException e) {
			throw new ElasticSearchException(e);
		}
		return this._writer.toString();
	}

	private void _writeSegmentCustom(int end)
			throws IOException, JsonGenerationException
	{
		final int[] escCodes = _outputEscapes;
		final int maxNonEscaped = (_maximumNonEscapedChar < 1) ? 0xFFFF : _maximumNonEscapedChar;
		final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
		final CharacterEscapes customEscapes = _characterEscapes;

		int ptr = 0;
		int escCode = 0;
		int start = ptr;

		output_loop:
		while (ptr < end) {
			// Fast loop for chars not needing escaping
			char c;
			while (true) {
				c = _outputBuffer[ptr];
				if (c < escLimit) {
					escCode = escCodes[c];
					if (escCode != 0) {
						break;
					}
				} else if (c > maxNonEscaped) {
					escCode = CharacterEscapes.ESCAPE_STANDARD;
					break;
				} else {
					if ((_currentEscape = customEscapes.getEscapeSequence(c)) != null) {
						escCode = CharacterEscapes.ESCAPE_CUSTOM;
						break;
					}
				}
				if (++ptr >= end) {
					break;
				}
			}
			int flushLen = (ptr - start);
			if (flushLen > 0) {
				_writer.write(_outputBuffer, start, flushLen);
				if (ptr >= end) {
					break output_loop;
				}
			}
			++ptr;
			start = _prependOrWriteCharacterEscape(_outputBuffer, ptr, end, c, escCode);
		}
	}

    /*
    /**********************************************************
    /* Implementations for other methods
    /**********************************************************
     */
	/**
	 * Method called to write "long strings", strings whose length exceeds
	 * output buffer length.
	 */
	private void _writeLongString(String text) throws IOException
	{
		// First things first: let's flush the buffer to get some more room
		_flushBuffer();

		// Then we can write
		final int textLen = text.length();
		int offset = 0;
		do {
			int max = _outputEnd;
			int segmentLen = ((offset + max) > textLen)
					? (textLen - offset) : max;
			text.getChars(offset, offset+segmentLen, _outputBuffer, 0);
			if (_characterEscapes != null) {
				_writeSegmentCustom(segmentLen);
			} else if (_maximumNonEscapedChar != 0) {
				_writeSegmentASCII(segmentLen, _maximumNonEscapedChar);
			} else {
				_writeSegment(segmentLen);
			}
			offset += segmentLen;
		} while (offset < textLen);
	}

	/**
	 * Method called to output textual context which has been copied
	 * to the output buffer prior to call. If any escaping is needed,
	 * it will also be handled by the method.
	 *<p>
	 * Note: when called, textual content to write is within output
	 * buffer, right after buffered content (if any). That's why only
	 * length of that text is passed, as buffer and offset are implied.
	 */
	private void _writeSegment(int end) throws IOException
	{
		final int[] escCodes = _outputEscapes;
		final int escLen = escCodes.length;

		int ptr = 0;
		int start = ptr;

		output_loop:
		while (ptr < end) {
			// Fast loop for chars not needing escaping
			char c;
			while (true) {
				c = _outputBuffer[ptr];
				if (c < escLen && escCodes[c] != 0) {
					break;
				}
				if (++ptr >= end) {
					break;
				}
			}

			// Ok, bumped into something that needs escaping.
            /* First things first: need to flush the buffer.
             * Inlined, as we don't want to lose tail pointer
             */
			int flushLen = (ptr - start);
			if (flushLen > 0) {
				_writer.write(_outputBuffer, start, flushLen);
				if (ptr >= end) {
					break output_loop;
				}
			}
			++ptr;
			// So; either try to prepend (most likely), or write directly:
			start = _prependOrWriteCharacterEscape(_outputBuffer, ptr, end, c, escCodes[c]);
		}
	}

	private void _writeSegmentASCII(int end, final int maxNonEscaped)
			throws IOException, JsonGenerationException
	{
		final int[] escCodes = _outputEscapes;
		final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);

		int ptr = 0;
		int escCode = 0;
		int start = ptr;

		output_loop:
		while (ptr < end) {
			// Fast loop for chars not needing escaping
			char c;
			while (true) {
				c = _outputBuffer[ptr];
				if (c < escLimit) {
					escCode = escCodes[c];
					if (escCode != 0) {
						break;
					}
				} else if (c > maxNonEscaped) {
					escCode = CharacterEscapes.ESCAPE_STANDARD;
					break;
				}
				if (++ptr >= end) {
					break;
				}
			}
			int flushLen = (ptr - start);
			if (flushLen > 0) {
				_writer.write(_outputBuffer, start, flushLen);
				if (ptr >= end) {
					break output_loop;
				}
			}
			++ptr;
			start = _prependOrWriteCharacterEscape(_outputBuffer, ptr, end, c, escCode);
		}
	}


    /*
    /**********************************************************
    /* Internal methods, low-level writing; text, default
    /**********************************************************
     */

	public void _writeString(String text) throws IOException
	{

		int len = text.length();
		if (len > _outputEnd) { // Let's reserve space for entity at begin/end
			_writeLongString(text);
			return;
		}

		// Ok: we know String will fit in buffer ok
		// But do we need to flush first?
		if ((_outputTail + len) > _outputEnd) {
			_flushBuffer();
		}
		text.getChars(0, len, _outputBuffer, _outputTail);

		if (_characterEscapes != null) {
			_writeStringCustom(len);
		} else if (_maximumNonEscapedChar != 0) {
			_writeStringASCII(len, _maximumNonEscapedChar);
		} else {
			_writeString2(len);
		}
	}
 /*
    /**********************************************************
    /* Internal methods, low-level writing, text segment
    /* with additional escaping (ASCII or such)
    /**********************************************************
     */

	/* Same as "_writeString2()", except needs additional escaping
	 * for subset of characters
	 */
	private void _writeStringASCII(final int len, final int maxNonEscaped)
			throws IOException, JsonGenerationException
	{
		// And then we'll need to verify need for escaping etc:
		int end = _outputTail + len;
		final int[] escCodes = _outputEscapes;
		final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
		int escCode = 0;

		output_loop:
		while (_outputTail < end) {
			char c;
			// Fast loop for chars not needing escaping
			escape_loop:
			while (true) {
				c = _outputBuffer[_outputTail];
				if (c < escLimit) {
					escCode = escCodes[c];
					if (escCode != 0) {
						break escape_loop;
					}
				} else if (c > maxNonEscaped) {
					escCode = CharacterEscapes.ESCAPE_STANDARD;
					break escape_loop;
				}
				if (++_outputTail >= end) {
					break output_loop;
				}
			}
			int flushLen = (_outputTail - _outputHead);
			if (flushLen > 0) {
				_writer.write(_outputBuffer, _outputHead, flushLen);
			}
			++_outputTail;
			_prependOrWriteCharacterEscape(c, escCode);
		}
	}

	public void _writeString2(final int len) throws IOException
	{
		// And then we'll need to verify need for escaping etc:
		final int end = _outputTail + len;
		final int[] escCodes = _outputEscapes;
		final int escLen = escCodes.length;

		output_loop:
		while (_outputTail < end) {
			// Fast loop for chars not needing escaping
			escape_loop:
			while (true) {
				char c = _outputBuffer[_outputTail];
				if (c < escLen && escCodes[c] != 0) {
					break escape_loop;
				}
				if (++_outputTail >= end) {
					break output_loop;
				}
			}

			// Ok, bumped into something that needs escaping.
            /* First things first: need to flush the buffer.
             * Inlined, as we don't want to lose tail pointer
             */
			int flushLen = (_outputTail - _outputHead);
			if (flushLen > 0) {
				_writer.write(_outputBuffer, _outputHead, flushLen);
			}
            /* In any case, tail will be the new start, so hopefully
             * we have room now.
             */
			char c = _outputBuffer[_outputTail++];
			_prependOrWriteCharacterEscape(c, escCodes[c]);
		}
	}






    /*
    /**********************************************************
    /* Internal methods, low-level writing, text segment
    /* with custom escaping (possibly coupling with ASCII limits)
    /**********************************************************
     */

	/* Same as "_writeString2()", except needs additional escaping
	 * for subset of characters
	 */
	private void _writeStringCustom(final int len)
			throws IOException, JsonGenerationException
	{
		// And then we'll need to verify need for escaping etc:
		int end = _outputTail + len;
		final int[] escCodes = _outputEscapes;
		final int maxNonEscaped = (_maximumNonEscapedChar < 1) ? 0xFFFF : _maximumNonEscapedChar;
		final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
		int escCode = 0;
		final CharacterEscapes customEscapes = _characterEscapes;

		output_loop:
		while (_outputTail < end) {
			char c;
			// Fast loop for chars not needing escaping
			escape_loop:
			while (true) {
				c = _outputBuffer[_outputTail];
				if (c < escLimit) {
					escCode = escCodes[c];
					if (escCode != 0) {
						break escape_loop;
					}
				} else if (c > maxNonEscaped) {
					escCode = CharacterEscapes.ESCAPE_STANDARD;
					break escape_loop;
				} else {
					if ((_currentEscape = customEscapes.getEscapeSequence(c)) != null) {
						escCode = CharacterEscapes.ESCAPE_CUSTOM;
						break escape_loop;
					}
				}
				if (++_outputTail >= end) {
					break output_loop;
				}
			}
			int flushLen = (_outputTail - _outputHead);
			if (flushLen > 0) {
				_writer.write(_outputBuffer, _outputHead, flushLen);
			}
			++_outputTail;
			_prependOrWriteCharacterEscape(c, escCode);
		}
	}
	/**
	 * This is the default set of escape codes, over 7-bit ASCII range
	 * (first 128 character codes), used for single-byte UTF-8 characters.
	 */
	protected final static int[] sOutputEscapes = CharTypes.get7BitOutputEscapes();
	/**
	 * Currently active set of output escape code definitions (whether
	 * and how to escape or not) for 7-bit ASCII range (first 128
	 * character codes). Defined separately to make potentially
	 * customizable
	 */
	protected int[] _outputEscapes = sOutputEscapes;

	/**
	 * Value between 128 (0x80) and 65535 (0xFFFF) that indicates highest
	 * Unicode code point that will not need escaping; or 0 to indicate
	 * that all characters can be represented without escaping.
	 * Typically used to force escaping of some portion of character set;
	 * for example to always escape non-ASCII characters (if value was 127).
	 *<p>
	 * NOTE: not all sub-classes make use of this setting.
	 */
	protected int _maximumNonEscapedChar;

	/**
	 * Definition of custom character escapes to use for generators created
	 * by this factory, if any. If null, standard data format specific
	 * escapes are used.
	 */
	protected CharacterEscapes _characterEscapes;
	private void _writeStringCustom(char[] text, int offset, int len)
			throws IOException, JsonGenerationException
	{
		len += offset; // -> len marks the end from now on
		final int[] escCodes = _outputEscapes;
		final int maxNonEscaped = (_maximumNonEscapedChar < 1) ? 0xFFFF : _maximumNonEscapedChar;
		final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
		final CharacterEscapes customEscapes = _characterEscapes;

		int escCode = 0;

		while (offset < len) {
			int start = offset;
			char c;

			while (true) {
				c = text[offset];
				if (c < escLimit) {
					escCode = escCodes[c];
					if (escCode != 0) {
						break;
					}
				} else if (c > maxNonEscaped) {
					escCode = CharacterEscapes.ESCAPE_STANDARD;
					break;
				} else {
					if ((_currentEscape = customEscapes.getEscapeSequence(c)) != null) {
						escCode = CharacterEscapes.ESCAPE_CUSTOM;
						break;
					}
				}
				if (++offset >= len) {
					break;
				}
			}

			// Short span? Better just copy it to buffer first:
			int newAmount = offset - start;
			if (newAmount < SHORT_WRITE) {
				// Note: let's reserve room for escaped char (up to 6 chars)
				if ((_outputTail + newAmount) > _outputEnd) {
					_flushBuffer();
				}
				if (newAmount > 0) {
					System.arraycopy(text, start, _outputBuffer, _outputTail, newAmount);
					_outputTail += newAmount;
				}
			} else { // Nope: better just write through
				_flushBuffer();
				_writer.write(text, start, newAmount);
			}
			// Was this the end?
			if (offset >= len) { // yup
				break;
			}
			// Nope, need to escape the char.
			++offset;
			_appendCharacterEscape(c, escCode);
		}
	}

	protected void _flushBuffer() throws IOException {
		int len = _outputTail - _outputHead;
		if (len > 0) {
			int offset = _outputHead;
			_outputTail = _outputHead = 0;
			_writer.write(_outputBuffer, offset, len);
		}
	}



    /*
    /**********************************************************
    /* Internal methods, low-level writing, escapes
    /**********************************************************
     */

	/**
	 * Method called to try to either prepend character escape at front of
	 * given buffer; or if not possible, to write it out directly.
	 * Uses head and tail pointers (and updates as necessary)
	 */
	private void _prependOrWriteCharacterEscape(char ch, int escCode)
			throws IOException, JsonGenerationException
	{
		if (escCode >= 0) { // \\N (2 char)
			if (_outputTail >= 2) { // fits, just prepend
				int ptr = _outputTail - 2;
				_outputHead = ptr;
				_outputBuffer[ptr++] = '\\';
				_outputBuffer[ptr] = (char) escCode;
				return;
			}
			// won't fit, write
			char[] buf = _entityBuffer;
			if (buf == null) {
				buf = _allocateEntityBuffer();
			}
			_outputHead = _outputTail;
			buf[1] = (char) escCode;
			_writer.write(buf, 0, 2);
			return;
		}
		if (escCode != CharacterEscapes.ESCAPE_CUSTOM) { // std, \\uXXXX
			if (_outputTail >= 6) { // fits, prepend to buffer
				char[] buf = _outputBuffer;
				int ptr = _outputTail - 6;
				_outputHead = ptr;
				buf[ptr] = '\\';
				buf[++ptr] = 'u';
				// We know it's a control char, so only the last 2 chars are non-0
				if (ch > 0xFF) { // beyond 8 bytes
					int hi = (ch >> 8) & 0xFF;
					buf[++ptr] = HEX_CHARS[hi >> 4];
					buf[++ptr] = HEX_CHARS[hi & 0xF];
					ch &= 0xFF;
				} else {
					buf[++ptr] = '0';
					buf[++ptr] = '0';
				}
				buf[++ptr] = HEX_CHARS[ch >> 4];
				buf[++ptr] = HEX_CHARS[ch & 0xF];
				return;
			}
			// won't fit, flush and write
			char[] buf = _entityBuffer;
			if (buf == null) {
				buf = _allocateEntityBuffer();
			}
			_outputHead = _outputTail;
			if (ch > 0xFF) { // beyond 8 bytes
				int hi = (ch >> 8) & 0xFF;
				int lo = ch & 0xFF;
				buf[10] = HEX_CHARS[hi >> 4];
				buf[11] = HEX_CHARS[hi & 0xF];
				buf[12] = HEX_CHARS[lo >> 4];
				buf[13] = HEX_CHARS[lo & 0xF];
				_writer.write(buf, 8, 6);
			} else { // We know it's a control char, so only the last 2 chars are non-0
				buf[6] = HEX_CHARS[ch >> 4];
				buf[7] = HEX_CHARS[ch & 0xF];
				_writer.write(buf, 2, 6);
			}
			return;
		}
		String escape;

		if (_currentEscape == null) {
			escape = _characterEscapes.getEscapeSequence(ch).getValue();
		} else {
			escape = _currentEscape.getValue();
			_currentEscape = null;
		}
		int len = escape.length();
		if (_outputTail >= len) { // fits in, prepend
			int ptr = _outputTail - len;
			_outputHead = ptr;
			escape.getChars(0, len, _outputBuffer, ptr);
			return;
		}
		// won't fit, write separately
		_outputHead = _outputTail;
		_writer.write(escape);
	}

	/**
	 * Method called to try to either prepend character escape at front of
	 * given buffer; or if not possible, to write it out directly.
	 *
	 * @return Pointer to start of prepended entity (if prepended); or 'ptr'
	 *   if not.
	 */
	private int _prependOrWriteCharacterEscape(char[] buffer, int ptr, int end,
											   char ch, int escCode)
			throws IOException, JsonGenerationException
	{
		if (escCode >= 0) { // \\N (2 char)
			if (ptr > 1 && ptr < end) { // fits, just prepend
				ptr -= 2;
				buffer[ptr] = '\\';
				buffer[ptr+1] = (char) escCode;
			} else { // won't fit, write
				char[] ent = _entityBuffer;
				if (ent == null) {
					ent = _allocateEntityBuffer();
				}
				ent[1] = (char) escCode;
				_writer.write(ent, 0, 2);
			}
			return ptr;
		}
		if (escCode != CharacterEscapes.ESCAPE_CUSTOM) { // std, \\uXXXX
			if (ptr > 5 && ptr < end) { // fits, prepend to buffer
				ptr -= 6;
				buffer[ptr++] = '\\';
				buffer[ptr++] = 'u';
				// We know it's a control char, so only the last 2 chars are non-0
				if (ch > 0xFF) { // beyond 8 bytes
					int hi = (ch >> 8) & 0xFF;
					buffer[ptr++] = HEX_CHARS[hi >> 4];
					buffer[ptr++] = HEX_CHARS[hi & 0xF];
					ch &= 0xFF;
				} else {
					buffer[ptr++] = '0';
					buffer[ptr++] = '0';
				}
				buffer[ptr++] = HEX_CHARS[ch >> 4];
				buffer[ptr] = HEX_CHARS[ch & 0xF];
				ptr -= 5;
			} else {
				// won't fit, flush and write
				char[] ent = _entityBuffer;
				if (ent == null) {
					ent = _allocateEntityBuffer();
				}
				_outputHead = _outputTail;
				if (ch > 0xFF) { // beyond 8 bytes
					int hi = (ch >> 8) & 0xFF;
					int lo = ch & 0xFF;
					ent[10] = HEX_CHARS[hi >> 4];
					ent[11] = HEX_CHARS[hi & 0xF];
					ent[12] = HEX_CHARS[lo >> 4];
					ent[13] = HEX_CHARS[lo & 0xF];
					_writer.write(ent, 8, 6);
				} else { // We know it's a control char, so only the last 2 chars are non-0
					ent[6] = HEX_CHARS[ch >> 4];
					ent[7] = HEX_CHARS[ch & 0xF];
					_writer.write(ent, 2, 6);
				}
			}
			return ptr;
		}
		String escape;
		if (_currentEscape == null) {
			escape = _characterEscapes.getEscapeSequence(ch).getValue();
		} else {
			escape = _currentEscape.getValue();
			_currentEscape = null;
		}
		int len = escape.length();
		if (ptr >= len && ptr < end) { // fits in, prepend
			ptr -= len;
			escape.getChars(0, len, buffer, ptr);
		} else { // won't fit, write separately
			_writer.write(escape);
		}
		return ptr;
	}

	/**
	 * Method called to append escape sequence for given character, at the
	 * end of standard output buffer; or if not possible, write out directly.
	 */
	private void _appendCharacterEscape(char ch, int escCode)
			throws IOException, JsonGenerationException
	{
		if (escCode >= 0) { // \\N (2 char)
			if ((_outputTail + 2) > _outputEnd) {
				_flushBuffer();
			}
			_outputBuffer[_outputTail++] = '\\';
			_outputBuffer[_outputTail++] = (char) escCode;
			return;
		}
		if (escCode != CharacterEscapes.ESCAPE_CUSTOM) { // std, \\uXXXX
			if ((_outputTail + 5) >= _outputEnd) {
				_flushBuffer();
			}
			int ptr = _outputTail;
			char[] buf = _outputBuffer;
			buf[ptr++] = '\\';
			buf[ptr++] = 'u';
			// We know it's a control char, so only the last 2 chars are non-0
			if (ch > 0xFF) { // beyond 8 bytes
				int hi = (ch >> 8) & 0xFF;
				buf[ptr++] = HEX_CHARS[hi >> 4];
				buf[ptr++] = HEX_CHARS[hi & 0xF];
				ch &= 0xFF;
			} else {
				buf[ptr++] = '0';
				buf[ptr++] = '0';
			}
			buf[ptr++] = HEX_CHARS[ch >> 4];
			buf[ptr++] = HEX_CHARS[ch & 0xF];
			_outputTail = ptr;
			return;
		}
		String escape;
		if (_currentEscape == null) {
			escape = _characterEscapes.getEscapeSequence(ch).getValue();
		} else {
			escape = _currentEscape.getValue();
			_currentEscape = null;
		}
		int len = escape.length();
		if ((_outputTail + len) > _outputEnd) {
			_flushBuffer();
			if (len > _outputEnd) { // very very long escape; unlikely but theoretically possible
				_writer.write(escape);
				return;
			}
		}
		escape.getChars(0, len, _outputBuffer, _outputTail);
		_outputTail += len;
	}

	private char[] _allocateEntityBuffer()
	{
		char[] buf = new char[14];
		// first 2 chars, non-numeric escapes (like \n)
		buf[0] = '\\';
		// next 6; 8-bit escapes (control chars mostly)
		buf[2] = '\\';
		buf[3] = 'u';
		buf[4] = '0';
		buf[5] = '0';
		// last 6, beyond 8 bits
		buf[8] = '\\';
		buf[9] = 'u';
		_entityBuffer = buf;
		return buf;
	}


}
