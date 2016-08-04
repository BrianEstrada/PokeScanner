
package com.pokescanner.objects;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Created by Brian on 7/22/2016.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"Name","filtered"}, callSuper = false)
public class FilterItem extends RealmObject{
    @PrimaryKey
    int Number;
    String Name;
    boolean filtered;

    public FilterItem() {}

    public FilterItem(int number) {
        setNumber(number);
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("number", Number);
        result.put("name", Name);
        result.put("filtered", filtered);
        return result;
    }
}
