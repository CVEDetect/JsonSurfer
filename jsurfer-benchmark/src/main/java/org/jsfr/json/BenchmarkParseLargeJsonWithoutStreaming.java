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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Threads(1)
@Fork(1)
@State(Scope.Benchmark)
public class BenchmarkParseLargeJsonWithoutStreaming {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkParseLargeJsonWithoutStreaming.class);

    private Gson gson;
    private ObjectMapper om;
    private String json;

    @Setup
    public void setup() throws IOException {
        gson = new GsonBuilder().create();
        om = new ObjectMapper();
        json = Resources.toString(Resources.getResource("allthethings.json"), StandardCharsets.UTF_8);
    }

    @Benchmark
    public void benchmarkRawGson(Blackhole blackhole) {
        JsonObject root = gson.fromJson(json, JsonObject.class);
        JsonObject builders = root.getAsJsonObject("builders");
        for (Map.Entry<String, JsonElement> entry : builders.entrySet()) {
            JsonElement value = entry.getValue().getAsJsonObject().getAsJsonObject("properties").get("branch");
            blackhole.consume(value);
            LOGGER.trace("json element: {}", value);
        }
    }

    @Benchmark
    public void benchmarkRawJackson(Blackhole blackhole) throws IOException {
        JsonNode node = om.readTree(json);
        Iterator<JsonNode> iterator = node.get("builders").elements();
        while (iterator.hasNext()) {
            JsonNode value = iterator.next().get("properties").get("branch");
            LOGGER.trace("json element: {}", value);
            blackhole.consume(value);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkParseLargeJsonWithoutStreaming.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

}
