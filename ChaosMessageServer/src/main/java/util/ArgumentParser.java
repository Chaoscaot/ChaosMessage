package util;

import java.util.HashMap;
import java.util.Map;

public class ArgumentParser {

    public static ArgumentMapping parse(ArgumentMapper map, String[] args) {
        Map<String, ArgumentMapper.Argument> mapping = map.get();
        Map<String, String> parsed = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if(args[i].startsWith(map.getStart())) {
                String toSearch = args[i].substring(1).toLowerCase();
                ArgumentMapper.Argument type = mapping.get(toSearch);
                if(type.getType() == ArgumentMapper.Argument.Type.FLAG)
                    parsed.put(toSearch, null);
                else {
                    i++;
                    if(args[i].startsWith(map.getStart()))
                        parsed.put(toSearch, type.getDefaultValue());
                    else
                        parsed.put(toSearch, args[i]);
                }
            }
        }
        mapping.entrySet().forEach(e -> {
            if(!parsed.containsKey(e.getKey()))
                parsed.put(e.getKey(), e.getValue().getDefaultValue());
        });
        return new ArgumentMapping(parsed);
    }

    public static class ArgumentMapping {
        private final Map<String, String> mappings;

        private ArgumentMapping(Map<String, String> mappings) {
            this.mappings = mappings;
        }

        public boolean hasKey(String key) {
            return mappings.containsKey(key);
        }

        public String getKey(String key) {
            return mappings.get(key);
        }
    }
}
