package com.mantao.star;

/**
 * Memetakan rawLabel hasil WasteClassifier (label ImageNet, misal "water bottle")
 * menjadi info yang lebih "manusiawi" untuk ditampilkan di History: nama material,
 * nama proses daur ulang (flavor text), dan eco points.
 *
 * Urutan tabel PENTING — baris yang lebih spesifik harus dicek lebih dulu
 * daripada baris generic (misal "wine bottle" sebelum "bottle").
 */
public class EcoPointsMapper {

    public static class MaterialInfo {
        public final String displayName;
        public final String processName;
        public final int ecoPoints;

        public MaterialInfo(String displayName, String processName, int ecoPoints) {
            this.displayName = displayName;
            this.processName = processName;
            this.ecoPoints = ecoPoints;
        }
    }

    private static final String[][] MATERIAL_TABLE = {
            // {keyword, displayName, processName, ecoPoints}
            {"wine bottle", "Glass Bottle", "Vitro Melt", "80"},
            {"beer bottle", "Glass Bottle", "Vitro Melt", "80"},
            {"water bottle", "Plastic Bottle", "PET Cycle", "12"},
            {"pop bottle", "Plastic Bottle", "PET Cycle", "12"},
            {"pill bottle", "Plastic Bottle", "PET Cycle", "12"},
            {"carton", "Cardboard Box", "Pulp Renew", "45"},
            {"cardboard", "Cardboard Box", "Pulp Renew", "45"},
            {"tin can", "Aluminum Can", "Alu Cycle", "25"},
            {"pop can", "Aluminum Can", "Alu Cycle", "25"},
            {"envelope", "Paper Waste", "Pulp Renew", "15"},
            {"paper towel", "Paper Waste", "Pulp Renew", "10"},
            {"banana", "Banana Peel", "Compost Boost", "10"},
            {"orange", "Orange Peel", "Compost Boost", "10"},
            {"mushroom", "Food Scrap", "Compost Boost", "8"},
            {"corn", "Food Scrap", "Compost Boost", "8"},
    };

    public static MaterialInfo getInfo(String rawLabel, String category) {
        String lower = rawLabel.toLowerCase();
        for (String[] row : MATERIAL_TABLE) {
            if (lower.contains(row[0])) {
                return new MaterialInfo(row[1], row[2], Integer.parseInt(row[3]));
            }
        }
        // Fallback generic kalau labelnya gak ada di tabel spesifik
        if ("Organik".equalsIgnoreCase(category)) {
            return new MaterialInfo(capitalize(rawLabel), "Compost Cycle", 10);
        } else {
            return new MaterialInfo(capitalize(rawLabel), "Eco Cycle", 15);
        }
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}