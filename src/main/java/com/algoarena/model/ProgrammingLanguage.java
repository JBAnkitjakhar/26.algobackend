// src/main/java/com/algoarena/model/ProgrammingLanguage.java
package com.algoarena.model;

public enum ProgrammingLanguage {
    JAVA("java"),
    PYTHON("python"),
    JAVASCRIPT("javascript"),
    CPP("cpp"),
    C("c"),
    CSHARP("csharp"),
    GO("go"),
    RUST("rust"),
    KOTLIN("kotlin"),
    SWIFT("swift"),
    RUBY("ruby"),
    PHP("php"),
    TYPESCRIPT("typescript");

    private final String value;

    ProgrammingLanguage(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ProgrammingLanguage fromString(String value) {
        for (ProgrammingLanguage lang : ProgrammingLanguage.values()) {
            if (lang.value.equalsIgnoreCase(value)) {
                return lang;
            }
        }
        throw new IllegalArgumentException("Invalid language: " + value);
    }
}