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

import com.google.common.io.Resources;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import profilers.FlightRecordingProfiler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Threads(1)
@Fork(1)
@State(Scope.Benchmark)
public class BenchmarkParseLargeJson {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkParseLargeJson.class);

    @Param({"$.builders.*.properties.branch", "$.builders..properties.branch"})
    private String jsonPath;

    private JsonSurfer simpleSurfer;
    private JsonSurfer gsonSurfer;
    private JsonSurfer jacksonSurfer;
    private JsonSurfer fastjsonSurfer;
    private SurfingConfiguration surfingConfiguration;
    private String json;

    @Setup
    public void setup(final Blackhole blackhole) throws IOException {
        gsonSurfer = JsonSurferGson.INSTANCE;
        jacksonSurfer = JsonSurferJackson.INSTANCE;
        simpleSurfer = JsonSurferJsonSimple.INSTANCE;
        fastjsonSurfer = JsonSurferFastJson.INSTANCE;
        JsonPathListener blackHoleListener = new JsonPathListener() {
            @Override
            public void onValue(Object value, ParsingContext context) {
                LOGGER.trace("Properties: {}", value);
                blackhole.consume(value);
            }
        };
        surfingConfiguration = SurfingConfiguration.builder().bind(jsonPath, blackHoleListener).skipOverlappedPath().build();
        json = Resources.toString(Resources.getResource("allthethings.json"), Charset.forName("UTF-8"));
    }

    @Benchmark
    public Object benchmarkJsonSimpleWithJsonSurfer() {
        simpleSurfer.surf(json, surfingConfiguration);
        return null;
    }

    @Benchmark
    public Object benchmarkGsonWithJsonSurfer() {
        gsonSurfer.surf(json, surfingConfiguration);
        return null;
    }

    @Benchmark
    public Object benchmarkJacksonWithJsonSurfer() {
        jacksonSurfer.surf(json, surfingConfiguration);
        return null;
    }

    @Benchmark
    public Object benchmarkFastJsonWithJsonSurfer() {
        fastjsonSurfer.surf(json, surfingConfiguration);
        return null;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkParseLargeJson.class.getSimpleName())
//                .addProfiler(FlightRecordingProfiler.class)
                .build();
        new Runner(opt).run();
    }
}
