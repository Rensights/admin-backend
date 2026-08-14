package com.rensights.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes an admin's manual corrections back into the stored analysis payload.
 *
 * <p>The admin edits the mapped, display-ready fields — exactly what the user's report shows —
 * and those values are written back onto the snake_case keys
 * {@link AnalysisResultMapper} reads. That keeps the mapper the single definition of the
 * mapping: a corrected value renders identically on the admin review screen and in the user's
 * report, with no second "overrides" layer to keep in step.
 *
 * <p>Only keys present in the submitted edits are touched, so a partial submission never wipes
 * a field the form did not carry. Re-fetching from the analysis module replaces the payload and
 * therefore discards manual edits — that is intended: a re-fetch means "take the module's word".
 */
@Component
public class AnalysisResultEditor {

    /** Marks a payload as manually corrected; ignored by the mapper, read by the admin UI. */
    public static final String EDITED_AT_FIELD = "admin_edited_at";

    /** Mapped field -> the payload key the mapper reads it from. */
    private static final Map<String, String> SCALAR_FIELDS = scalarFields();

    /** Mapped comparable field -> payload key, for {@code listing_comparables[]}. */
    private static final Map<String, String> LISTING_FIELDS = Map.of(
        "buildingName", "building_name",
        "area", "area",
        "bedrooms", "bedrooms",
        "sizeDisplay", "size_display",
        "listedPriceDisplay", "listed_price_display",
        "pricePerSqftDisplay", "price_per_sqft_display",
        "listingUrl", "url"
    );

    /** Mapped comparable field -> payload key, for {@code transaction_comparables[]}. */
    private static final Map<String, String> TRANSACTION_FIELDS = Map.of(
        "buildingName", "building_name",
        "area", "area",
        "bedrooms", "bedrooms",
        "sizeDisplay", "size_display",
        "salePriceDisplay", "sale_price_display",
        "pricePerSqftDisplay", "price_per_sqft_display",
        "transactionDate", "transaction_date"
    );

    private static Map<String, String> scalarFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("buildingName", "building_name");
        fields.put("area", "area");
        fields.put("city", "city");
        fields.put("bedrooms", "bedrooms");
        fields.put("size", "size_sqft");
        fields.put("buildingStatus", "building_status");
        fields.put("marketGapPercentage", "market_gap_percentage");
        fields.put("marketDirectionLabel", "market_direction_label");
        fields.put("rentalYield", "rental_yield_estimate");
        fields.put("listedPrice", "listed_price_aed");
        fields.put("estimateRange", "our_price_estimate");
        fields.put("potentialSavings", "potential_savings");
        fields.put("pricePerSqft", "price_per_sqft");
        fields.put("marketPosition", "market_position");
        fields.put("dubaiComparison", "dubai_comparison");
        fields.put("furnishing", "furnishing");
        fields.put("developer", "developer");
        fields.put("view", "view");
        fields.put("serviceCharge", "service_charge");
        fields.put("nearestLandmark", "nearest_landmark");
        fields.put("buildingFeatures", "building_features");
        return fields;
    }

    private final ObjectMapper objectMapper;

    public AnalysisResultEditor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * The stored payload with the admin's corrections applied. The original node is not
     * modified; a new one is returned for the caller to persist.
     */
    public JsonNode applyEdits(JsonNode original, Map<String, Object> edits) {
        ObjectNode payload = original != null && original.isObject()
            ? original.deepCopy()
            : objectMapper.createObjectNode();

        if (edits == null || edits.isEmpty()) {
            return payload;
        }

        SCALAR_FIELDS.forEach((mapped, field) -> {
            if (edits.containsKey(mapped)) {
                payload.put(field, asText(edits.get(mapped)));
            }
        });

        if (edits.containsKey("valuationWarning")) {
            applyValuationWarning(payload, edits.get("valuationWarning"));
        }
        if (edits.containsKey("listingComparables")) {
            payload.set("listing_comparables",
                comparables(edits.get("listingComparables"), LISTING_FIELDS));
        }
        if (edits.containsKey("transactionComparables")) {
            payload.set("transaction_comparables",
                comparables(edits.get("transactionComparables"), TRANSACTION_FIELDS));
        }

        payload.put(EDITED_AT_FIELD, LocalDateTime.now().toString());
        return payload;
    }

    /** A warning cleared of both its title and message is removed rather than left blank. */
    private void applyValuationWarning(ObjectNode payload, Object value) {
        Map<String, Object> warning = asMap(value);
        String title = warning != null ? asText(warning.get("title")) : "";
        String message = warning != null ? asText(warning.get("message")) : "";

        if (title.isEmpty() && message.isEmpty()) {
            payload.remove("valuation_warning");
            return;
        }

        ObjectNode node = objectMapper.createObjectNode();
        node.put("title", title);
        node.put("message", message);
        payload.set("valuation_warning", node);
    }

    /**
     * The submitted comparables, rewritten to the payload's key names. The list replaces the
     * stored one wholesale, so the admin can drop or add rows. Rows with nothing filled in are
     * skipped.
     */
    private ArrayNode comparables(Object value, Map<String, String> fieldMap) {
        ArrayNode rows = objectMapper.createArrayNode();
        if (!(value instanceof List<?> submitted)) {
            return rows;
        }

        for (Object entry : submitted) {
            Map<String, Object> row = asMap(entry);
            if (row == null) {
                continue;
            }
            ObjectNode node = objectMapper.createObjectNode();
            boolean hasValue = false;
            for (Map.Entry<String, String> field : fieldMap.entrySet()) {
                String text = asText(row.get(field.getKey()));
                node.put(field.getValue(), text);
                hasValue |= !text.isEmpty();
            }
            if (hasValue) {
                rows.add(node);
            }
        }
        return rows;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    private String asText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
