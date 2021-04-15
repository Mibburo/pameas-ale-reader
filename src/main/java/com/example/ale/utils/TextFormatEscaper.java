package com.example.ale.utils;

import com.google.protobuf.ByteString;

final class TextFormatEscaper {
    private TextFormatEscaper() {}

    private interface ByteSequence {
        int size();

        byte byteAt(int offset);
    }

    /**
     * Escapes bytes in the format used in protocol buffer text format, which is the same as the
     * format used for C string literals. All bytes that are not printable 7-bit ASCII characters are
     * escaped, as well as backslash, single-quote, and double-quote characters. Characters for which
     * no defined short-hand escape sequence is defined will be escaped using 3-digit octal sequences.
     */
    static String escapeBytes(final ByteSequence input) {
        final StringBuilder builder = new StringBuilder(input.size());
        for (int i = 0; i < input.size(); i++) {
            final byte b = input.byteAt(i);
            switch (b) {
                // Java does not recognize \a or \v, apparently.
                case 0x07:
                    builder.append("\\a");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                case 0x0b:
                    builder.append("\\v");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\'':
                    builder.append("\\\'");
                    break;
                case '"':
                    builder.append("\\\"");
                    break;
                default:
                    // Only ASCII characters between 0x20 (space) and 0x7e (tilde) are
                    // printable.  Other byte values must be escaped.
                    if (b >= 0x20 && b <= 0x7e) {
                        builder.append((char) b);
                    } else {
                        builder.append('\\');
                        builder.append((char) ('0' + ((b >>> 6) & 3)));
                        builder.append((char) ('0' + ((b >>> 3) & 7)));
                        builder.append((char) ('0' + (b & 7)));
                    }
                    break;
            }
        }
        return builder.toString();
    }

    /**
     * Escapes bytes in the format used in protocol buffer text format, which is the same as the
     * format used for C string literals. All bytes that are not printable 7-bit ASCII characters are
     * escaped, as well as backslash, single-quote, and double-quote characters. Characters for which
     * no defined short-hand escape sequence is defined will be escaped using 3-digit octal sequences.
     */
    static String escapeBytes(final ByteString input) {
        return escapeBytes(
                new ByteSequence() {
                    @Override
                    public int size() {
                        return input.size();
                    }

                    @Override
                    public byte byteAt(int offset) {
                        return input.byteAt(offset);
                    }
                });
    }

    /** Like {@link #escapeBytes(ByteString)}, but used for byte array. */
    static String escapeBytes(final byte[] input) {
        return escapeBytes(
                new ByteSequence() {
                    @Override
                    public int size() {
                        return input.length;
                    }

                    @Override
                    public byte byteAt(int offset) {
                        return input[offset];
                    }
                });
    }

    /**
     * Like {@link #escapeBytes(ByteString)}, but escapes a text string. Non-ASCII characters are
     * first encoded as UTF-8, then each byte is escaped individually as a 3-digit octal escape. Yes,
     * it's weird.
     */
    static String escapeText(final String input) {
        return escapeBytes(ByteString.copyFromUtf8(input));
    }

    /** Escape double quotes and backslashes in a String for unicode output of a message. */
    static String escapeDoubleQuotesAndBackslashes(final String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
