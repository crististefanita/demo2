package atf.api.reporting.console;
@FunctionalInterface public interface AtfConsoleSink { void publish(AtfConsoleEvent event); static AtfConsoleSink noOp(){return event -> { };} }
