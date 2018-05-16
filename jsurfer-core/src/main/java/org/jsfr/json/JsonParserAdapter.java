/*
 * The MIT License
 *
 * Copyright (c) 2017 WANG Lingsong
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jsfr.json;

import java.io.InputStream;
import java.io.Reader;

/**
 * Created by Leo on 2015/4/2.
 */
public interface JsonParserAdapter {

    /**
     * Create and start a resumable parser
     *
     * @param reader  reader
     * @param context SurfingContext
     * @deprecated use {@link #parse(InputStream, SurfingContext)} instead
     */
    @Deprecated
    void parse(Reader reader, SurfingContext context);

    /**
     * Create and start a resumable parser
     *
     * @param json    json
     * @param context SurfingContext
     */
    void parse(String json, SurfingContext context);

    /**
     * Create and start a resumable parser
     *
     * @param inputStream inputStream
     * @param context     SurfingContext
     */
    void parse(InputStream inputStream, SurfingContext context);

    /**
     * Create a resumable parser
     *
     * @param reader  Json source
     * @param context Surfing context
     * @return Resumable Parser
     * @deprecated use {@link #createResumableParser(InputStream, SurfingContext)} instead
     */
    @Deprecated
    ResumableParser createResumableParser(Reader reader, SurfingContext context);

    /**
     * Create a resumable parser
     *
     * @param json    Json source
     * @param context Surfing context
     * @return Resumable parser
     */
    ResumableParser createResumableParser(String json, SurfingContext context);

    /**
     * Create a resumable parser
     *
     * @param json    Json source
     * @param context Surfing context
     * @return Resumable Parser
     */
    ResumableParser createResumableParser(InputStream json, SurfingContext context);

    /**
     * Create a NonBlockingParser
     *
     * @param context Surfing context
     * @return NonBlockingParser
     */
    NonBlockingParser createNonBlockingParser(SurfingContext context);

}
