/*
 * Copyright (c) 2017 Joao Paulo Fernandes Ventura. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */


package br.com.firebase.ui.databinding;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import android.databinding.ObservableArrayMap;
import android.databinding.ObservableMap;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public abstract class FirebaseArrayMap<K extends Object, V> extends ObservableArrayMap<K, V>
        implements ChildEventListener, CRUD<K, V> {

    private final DatabaseReference mDatabaseReference;

    public abstract Class<V> getType();

    private FirebaseArrayMap() throws IllegalArgumentException {
        throw new IllegalArgumentException();
    }

    public FirebaseArrayMap(@NonNull DatabaseReference databaseReference) {
        mDatabaseReference = databaseReference;
    }

    @Override
    public void addOnMapChangedCallback(OnMapChangedCallback<? extends ObservableMap<K, V>, K, V> listener) {
        super.addOnMapChangedCallback(listener);
        mDatabaseReference.addChildEventListener(this);
    }

    @Override
    public void removeOnMapChangedCallback(OnMapChangedCallback<? extends ObservableMap<K, V>, K, V> listener) {
        mDatabaseReference.removeEventListener(this);
        super.removeOnMapChangedCallback(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WorkerThread
    public V put(K key, V value) {
        try {
            return Tasks.await(create(key, value));
        } catch (ExecutionException e) {
            return null;
        } catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelled(DatabaseError error) {
        Log.e(FirebaseArrayMap.class.getSimpleName(), error.getMessage(), error.toException());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
        if (snapshot.exists()) {
            super.put((K) snapshot.getKey(), snapshot.getValue(getType()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
        if (snapshot.exists()) {
            super.put((K)snapshot.getKey(), snapshot.getValue(getType()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        super.remove(dataSnapshot.getKey());
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public Task<V> create(V value) {
        final Continuation<Void, V> onCreate = new Continuation<Void, V>() {
            @Override
            public V then(@NonNull Task<Void> task) throws Exception {
                return null;
            }
        };
        return mDatabaseReference.push().setValue(value).continueWith(onCreate);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public Task<V> create(K key, V value) {
        final V oldValue = get(key);
        final Continuation<Void, V> onCreate = new Continuation<Void, V>() {
            @Override
            public V then(@NonNull Task<Void> task) throws Exception {
                return oldValue;
            }
        };
        return mDatabaseReference.child(key.toString()).setValue(value).continueWith(onCreate);
    }

    @Override
    public Task<Void> createAll(SimpleArrayMap<? extends K, ? extends V> array) {
        // FIXME: Ensure capacity
        Collection<Task<V>> tasks = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            tasks.add(create(keyAt(i), array.valueAt(i)));
        }
        return Tasks.whenAll(tasks);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public Task<Void> createAll(Map<? extends K, ? extends V> map) {
        // FIXME: Ensure capacity
        Collection<Task<V>> tasks = new ArrayList<>(map.size());
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            tasks.add(create(entry.getKey(), entry.getValue()));
        }
        return Tasks.whenAll(tasks);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public V read(K key) {
        return get(key);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public Task<V> update(K key, V value) {
        return this.create(key, value);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public Task<V> delete(K key) {
        final V oldValue = get(key);
        final Continuation<Void, V> onDelete = new Continuation<Void, V>() {
            @Override
            public V then(@NonNull Task<Void> task) throws Exception {
                task.getResult();
                return oldValue;
            }
        };
        return mDatabaseReference.child(key.toString()).setValue(null).continueWith(onDelete);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public Task<Void> free() {
        final Continuation<Void, Void> onDelete = new Continuation<Void, Void>() {
            @Override
            public Void then(@NonNull Task<Void> task) throws Exception {
                Void result = task.getResult();
                FirebaseArrayMap.super.clear();
                return result;
            }
        };
        return mDatabaseReference.setValue(null).continueWith(onDelete);
    }

}
