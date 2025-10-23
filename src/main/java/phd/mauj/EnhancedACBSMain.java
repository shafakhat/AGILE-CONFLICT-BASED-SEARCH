package phd.mauj;

import java.io.IOException;
import java.util.Scanner;


// ============================================================================
// INTEGRATION WITH EXISTING DEMO
// ============================================================================

class EnhancedACBSMain extends ACBSMain{
    public static void main(String[] args) throws IOException {
        System.out.println("ENHANCED ACBS DEMO - WITH CONFLICT TESTING");
        System.out.println("==========================================\n");
		Scanner s = new Scanner(System.in);
        String ca = s.nextLine();
       
        // Run original simple demo first
        ACBSMain.demonstrateSmallExample();
        
        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("NOW RUNNING MODERATE CONFLICT SCENARIOS");
        System.out.println("=".repeat(70));
        
        // Run conflict scenarios
        if (args.length > 0 && args[0].equals("--conflict-test")) {
            ModerateConflictTester.runAllTests();
        } else {
            // Test just one scenario for quick demonstration
            ModerateConflictTester.testSingleScenario(ca);
			System.exit(0);
        }
        
        s.close();
    }
}