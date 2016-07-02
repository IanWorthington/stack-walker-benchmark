package fr.pingtimeout.scratch;

import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class FileAndLineNumberBenchmark {

    StackWalker stackWalker;

    @Setup
    public void buildStackWalker() {
        stackWalker = StackWalker.getInstance();
    }

    @Benchmark
    public Data[] exception() {
        return last5FramesUsingException(0);
    }

    private Data[] last5FramesUsingException(int methodDepth) {
        if (methodDepth < 5) {
            return last5FramesUsingException(methodDepth + 1);
        } else {
            StackTraceElement[] stackTrace = new Exception().getStackTrace();
            Data[] result = new Data[5];
            for (int i = 0; i < result.length; i++) {
                result[i] = new Data(
                        stackTrace[i].getFileName(),
                        stackTrace[i].getMethodName(),
                        stackTrace[i].getLineNumber());
            }
            return result;
        }
    }

    @Benchmark
    public List<Data> stackwalkerWithFileNameAndLineNumber() {
        return last5FramesUsingStackWalker(0, s -> new Data(s.getFileName(), s.getMethodName(), s.getLineNumber()));
    }

    @Benchmark
    public List<Data> stackwalkerWithExplicitStackTraceElement() {
        return last5FramesUsingStackWalker(0, s -> {
            StackTraceElement ste = s.toStackTraceElement();
            return new Data(ste.getFileName(), ste.getMethodName(), ste.getLineNumber());
        });
    }

    @Benchmark
    public List<Data> stackwalkerWithClassMethodAndBCI() {
        return last5FramesUsingStackWalker(0, s -> new Data(s.getClassName(), s.getMethodName(), s.getByteCodeIndex()));
    }

    @Benchmark
    public List<Data> stackwalkerWithClassAndBCI() {
        return last5FramesUsingStackWalker(0, s -> new Data(s.getClassName(), null, s.getByteCodeIndex()));
    }

    private List<Data> last5FramesUsingStackWalker(int methodDepth, Function<StackWalker.StackFrame, Data> builder) {
        if (methodDepth < 5) {
            return last5FramesUsingStackWalker(methodDepth + 1, builder);
        } else {
            return stackWalker.walk(s -> s.limit(5).map(builder).collect(toList()));
        }
    }

    static class Data {
        private final String fileNameOrClassName;
        private final String methodName;
        private final int lineNumberOrBci;

        Data(String fileNameOrClassName, String methodName, int lineNumberOrBci) {
            this.fileNameOrClassName = fileNameOrClassName;
            this.methodName = methodName;
            this.lineNumberOrBci = lineNumberOrBci;
        }
    }
}
