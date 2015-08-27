/*
 *    Copyright 2015 TedXiong <xiong-wei@hotmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.android.ted.gank.db;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Ted on 2015/8/27.
 */
public class GankRealmHelper {
    public static Realm getRealm(Context context){
        RealmConfiguration configuration = new RealmConfiguration.Builder(context)
                .name("gank.io")
                .schemaVersion(0)
                .build();
        Realm.migrateRealm(configuration, new GankMigration());
        Realm realm = Realm.getInstance(configuration);
        return realm;
    }
}
