package util;

import java.util.HashMap;
import java.util.Map;

public class ArgumentMapper {

    private HashMap<String, Argument> mapping = new HashMap<>();
    private final String start;

    public ArgumentMapper(String start) {
        this.start = start;
    }

    public void addFlag(String key) {
        mapping.put(key, new Argument("", Argument.Type.FLAG));
    }

    public void addValue(String key) {
        mapping.put(key, new Argument("", Argument.Type.VALUE));
    }

    public void addValue(String key, String defaultValue) {
        mapping.put(key, new Argument(defaultValue, Argument.Type.VALUE));
    }

    protected Map<String, Argument> get() {
        return mapping;
    }

    protected String getStart() {
        return start;
    }
    protected static class Argument {
        private final String defaultValue;
        private final Type type;

        public Argument(String defaultValue, Type type) {
            this.defaultValue = defaultValue;
            this.type = type;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public Type getType() {
            return type;
        }

        protected enum Type {
            VALUE,
            FLAG
        }
    }
}
