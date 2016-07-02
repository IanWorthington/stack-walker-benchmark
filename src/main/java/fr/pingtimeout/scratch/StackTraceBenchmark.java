package fr.pingtimeout.scratch;

import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * The {@link StackTraceBenchmark} class measures the time it takes to get the 5 last stack frames at a given point in
 * time.  It compares the classic approach (builing and exception) with the new {@link StackWalker} APIs.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class StackTraceBenchmark {

    StackWalker stackWalker;

    @Setup
    public void buildStackWalker() {
        stackWalker = StackWalker.getInstance();
    }

    @Benchmark
    public StackTraceElement[] exception() {
        return last5FramesUsingException(0);
    }

    private StackTraceElement[] last5FramesUsingException(int methodDepth) {
        if(methodDepth < 5) {
            return last5FramesUsingException(methodDepth + 1);
        } else {
            return Arrays.copyOfRange(new Exception().getStackTrace(), 0, 5);
        }
    }

    @Benchmark
    public List<StackWalker.StackFrame> stackwalker() {
        return last5FramesUsingStackWalker(0);
    }

    private List<StackWalker.StackFrame> last5FramesUsingStackWalker(int methodDepth) {
        if(methodDepth < 5) {
            return last5FramesUsingStackWalker(methodDepth + 1);
        } else {
            return stackWalker.walk(s -> s.limit(5).collect(toList()));
        }
    }
}
