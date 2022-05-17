package io.ballerina.wsdl.cmd;

import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.util.Collections;

/**
 * Utility class for WSDL client generation command line tool.
 */
public class Utils {
    /**
     * This util method is used to generate {@code Diagnostic} for graphql command errors.
     */
    public static GraphqlDiagnostic constructGraphqlDiagnostic(String code, String message, DiagnosticSeverity severity,
                                                               Location location, Object... args) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(code, message, severity);
        if (location == null) {
            location = new NullLocation();
        }
        return new GraphqlDiagnostic(diagnosticInfo, location, Collections.emptyList(), args);
    }

    /**
     * This {@code NullLocation} represents the null location allocation for scenarios which has no location.
     */
    public static class NullLocation implements Location {
        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from("", from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }

}
