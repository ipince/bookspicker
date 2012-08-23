package com.bookspicker.shared;

public enum Location {
	
	BAKER, BEXLEY, BURTON_CONNER,
	EAST_CAMPUS,
	MCCORMICK,
	NEW_HOUSE, NEXT_HOUSE,
	PHOENIX_GROUP,
	RANDOM,
	SENIOR_HOUSE, SIMMONS, STUDENT_CENTER;
	
	private static final int[][] DISTANCES = new int[][] {
		//         Baker | Bexley | BC | EC | McCormick | Ashdown | New | Next | Random | Senior | Simmons | Student Center
		new int[] {  0,       4,    2,   11,     2,        14,       5,     7,    11,      11,      11,             6 }, // Baker
		new int[] {	 4,       0,    6,    9,     3,         9,       9,    12,     6,       9,      10,             1 }, // Bexley
		new int[] {	 2,	      6,    0,   14,     4,        12,       2,     5,    13,      14,       8,             7 }, // BC
		new int[] {	11,       9,   14,    0,     9,        17,      17,    18,    14,       1,      18,            10 }, // EC
		new int[] {	 2,       3,    4,    9,     0,        12,       7,     8,    10,       9,      13,             4 }, // McCormick
		new int[] {	14,       9,   12,   17,    12,         0,      10,    11,     8,      19,       8,             8 }, // Ashdown
		new int[] {	 5,       9,    2,   17,     7,        10,       0,     5,    16,      17,       5,            10 }, // New
		new int[] {	 7,      12,    5,   18,     8,        11,       5,     0,    18,      18,       7,            12 }, // Next
		new int[] {	11,       6,   13,   14,    10,         8,      16,    18,     0,      16,      12,             6 }, // Random
		new int[] {	11,       9,   14,    1,     9,        19,      17,    18,    16,       0,      19,            10 }, // Senior
		new int[] {	11,      10,    8,   18,    13,         8,       5,     7,    12,      19,       0,             9 }, // Simmons
		new int[] {	 6,       1,    7,   10,     4,         8,      10,    12,     6,      10,       9,             0 }  // Student Center
	};
	
	public static int getDistance(Location loc1, Location loc2) {
		return DISTANCES[loc1.ordinal()][loc2.ordinal()];
	}
	
	public String getDisplayName() {
		switch(this) {
		case BAKER: return "Baker";
		case BEXLEY: return "Bexley";
		case BURTON_CONNER: return "BC";
		case EAST_CAMPUS: return "EAsT camPUS";
		case MCCORMICK: return "McCormick";
		case NEW_HOUSE: return "New House";
		case NEXT_HOUSE: return "Next House";
		case PHOENIX_GROUP: return "Phoenix Group";
		case RANDOM: return "Random Hall";
		case SENIOR_HOUSE: return "Senior Haus";
		case SIMMONS: return "Simmons";
		case STUDENT_CENTER: return "Student Center";
		}
		return "";
	}

}
