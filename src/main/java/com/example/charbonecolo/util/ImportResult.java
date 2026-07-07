package com.example.charbonecolo.util;

import java.util.Collections;
import java.util.List;

public class ImportResult {
    private final int successCount;
    private final int errorCount;
    private final int skippedCount;
    private final List<String> errors;
    private final List<String> warnings;

    public ImportResult(int successCount, int errorCount, int skippedCount, 
                        List<String> errors, List<String> warnings) {
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.skippedCount = skippedCount;
        this.errors = errors != null ? List.copyOf(errors) : Collections.emptyList();
        this.warnings = warnings != null ? List.copyOf(warnings) : Collections.emptyList();
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}
