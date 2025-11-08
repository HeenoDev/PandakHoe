package weeno.pandakhoe.crops;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CropType {
    WHEAT(1),
    CARROT(2),
    POTATO(3),
    MUTATED_WHEAT(11),
    MUTATED_CARROT(12),
    MUTATED_POTATO(13);

    private final int id;

    public static CropType fromId(int id) {
        for (CropType t : values()) {
            if (t.id == id) return t;
        }
        return null;
    }

    public boolean isMutated() {
        return id >= 10;
    }
}