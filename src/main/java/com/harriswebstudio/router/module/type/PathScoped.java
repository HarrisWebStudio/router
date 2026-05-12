package com.harriswebstudio.router.module.type;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Getter
@SuppressWarnings("unchecked")
public abstract class PathScoped {

    protected final Set<Pattern> pathRegexes = new HashSet<>();
    protected final Set<Pattern> exemptPathRegexes = new HashSet<>();

    public final <T extends PathScoped> T paths(String... pathRegexes) {
        for (String pathRegex : pathRegexes)
            this.pathRegexes.add(Pattern.compile(pathRegex));

        return (T) this;
    }

    public final <T extends PathScoped> T exemptPaths(String... exemptPathRegexes) {
        for (String exemptPathRegex : exemptPathRegexes)
            this.exemptPathRegexes.add(Pattern.compile(exemptPathRegex));

        return (T) this;
    }

    public boolean pathMatches(String path) {
        if (exemptPathRegexes.stream().anyMatch(pattern -> pattern.matcher(path).matches()))
            return false;

        if (pathRegexes.isEmpty())
            return true;

        return pathRegexes.stream().anyMatch(pattern -> pattern.matcher(path).matches());
    }

}