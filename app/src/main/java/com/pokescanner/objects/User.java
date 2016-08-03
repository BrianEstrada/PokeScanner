
package com.pokescanner.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * Created by Brian on 7/22/2016.
 */
@Data
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false,exclude = {"token","status","authType","lastScan"})
public class User extends RealmObject{
    static public final int PTC = 0;
    static public final int GOOGLE = 1;
    static public final int STATUS_UNKNOWN  = 10;
    static public final int STATUS_INVALID  = 11;
    static public final int STATUS_VALID  = 12;

    @PrimaryKey
    String username;
    String password;
    GoogleAuthToken token;
    int authType;
    int status = 10;
    int accountColor = -1;

    public User() {}

    public User(String username,String password,GoogleAuthToken token,int authType,int status) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.authType = authType;
        this.status = status;
    }
}
