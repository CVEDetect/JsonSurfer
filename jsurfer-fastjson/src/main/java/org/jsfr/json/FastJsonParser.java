/*
 * MIT License
 *
 * Copyright (c) 2019 WANG Lingsong
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jsfr.json;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.JSONLexerBase;
import com.alibaba.fastjson.parser.JSONReaderScanner;
import com.alibaba.fastjson.parser.JSONScanner;
import org.jsfr.json.exception.JsonSurfingException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import static com.alibaba.fastjson.parser.JSONToken.*;

/**
 * Created by Leo on 2017/3/31.
 */
public class FastJsonParser implements JsonParserAdapter {

    private static class FastJsonResumableParser implements ResumableParser {

        private JSONLexerBase lexer;
        private SurfingContext context;
        private StaticPrimitiveHolder staticPrimitiveHolder;

        public FastJsonResumableParser(JSONLexerBase lexer, SurfingContext context, StaticPrimitiveHolder staticPrimitiveHolder) {
            this.lexer = lexer;
            this.context = context;
            this.staticPrimitiveHolder = staticPrimitiveHolder;
        }

        @Override
        public void parse() {
            context.startJSON();
            doParse();
        }

        @Override
        public boolean resume() {
            try {
                if (!context.isPaused()) {
                    return false;
                }
                context.resume();
                doParse();
                return true;
            } catch (Exception e) {
                context.getConfig().getErrorHandlingStrategy().handleParsingException(e);
                return false;
            }
        }

        private void doParse() {

            try {
                String tempString = null;

                while (!lexer.isEOF() && !context.shouldBreak()) {
                    lexer.nextToken();
                    int token = lexer.token();
                    //System.out.println("token: " + token);
                    if (tempString != null) {
                        if (token == COLON) {
                            context.startObjectEntry(tempString);
                        } else {
                            context.primitive(staticPrimitiveHolder.withValue(tempString));
                        }
                        tempString = null;
                    }
                    switch (token) {
                        case SET:
                        case TREE_SET:
                        case NEW:
                        case COMMA:
                        case COLON:
                            break;
                        case LBRACKET:
                            context.startArray();
                            break;
                        case RBRACKET:
                            context.endArray();
                            break;
                        case LBRACE:
                            context.startObject();
                            break;
                        case RBRACE:
                            context.endObject();
                            break;
                        case LITERAL_INT:
                            context.primitive(staticPrimitiveHolder.withValue(lexer.longValue()));
                            break;
                        case LITERAL_FLOAT:
                            // Number value = lexer.decimalValue(lexer.isEnabled(Feature.UseBigDecimal));
                            context.primitive(staticPrimitiveHolder.withValue(lexer.doubleValue()));
                            break;
                        case IDENTIFIER:
                        case LITERAL_STRING:
                            tempString = lexer.stringVal();
                            break;
                        case NULL:
                        case UNDEFINED:
                            context.primitive(staticPrimitiveHolder.withValue(null));
                            break;
                        case TRUE:
                            context.primitive(staticPrimitiveHolder.withValue(true));
                            break;
                        case FALSE:
                            context.primitive(staticPrimitiveHolder.withValue(false));
                            break;
                        case EOF:
                            context.endJSON();
                            break;
                        case ERROR:
                        default:
                            throw new JSONException("syntax error, " + lexer.info());
                    }
                }

                if (tempString != null) {
                    context.primitive(staticPrimitiveHolder.withValue(tempString));
                }

                if (context.getConfig().isCloseParserOnStop() && context.isStopped()) {
                    lexer.close();
                }

            } catch (Exception e) {
                context.getConfig().getErrorHandlingStrategy().handleParsingException(e);
            }

        }
    }

    public final static FastJsonParser INSTANCE = new FastJsonParser();

    private FastJsonParser() {
    }

    @Override
    public void parse(Reader reader, SurfingContext context) {
        createResumableParser(reader, context).parse();
    }

    @Override
    public void parse(String json, SurfingContext context) {
        createResumableParser(json, context).parse();
    }

    @Override
    public void parse(InputStream inputStream, SurfingContext context) {
        createResumableParser(inputStream, context).parse();
    }

    @Override
    public ResumableParser createResumableParser(Reader reader, SurfingContext context) {
        return new FastJsonResumableParser(new JSONReaderScanner(reader), context, new StaticPrimitiveHolder());
    }

    @Override
    public ResumableParser createResumableParser(String json, SurfingContext context) {
        return new FastJsonResumableParser(new JSONScanner(json), context, new StaticPrimitiveHolder());
    }

    @Override
    public ResumableParser createResumableParser(InputStream json, SurfingContext context) {
        return new FastJsonResumableParser(new JSONReaderScanner(new InputStreamReader(json, context.getConfig().getParserCharset())), context, new StaticPrimitiveHolder());
    }

    @Override
    public NonBlockingParser createNonBlockingParser(SurfingContext context) {
        throw new UnsupportedOperationException("Unsupported");
    }

}
