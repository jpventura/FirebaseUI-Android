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

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirebaseObservableMap<K, V> extends ObservableArrayMap<K, V> implements
        ChildEventListener, CRUD<K, V> {

    private final DatabaseReference mDatabaseReference;

    private FirebaseObservableMap() throws IllegalArgumentException {
        throw new IllegalArgumentException();
    }

    public FirebaseObservableMap(@NonNull DatabaseReference databaseReference) {
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
    @WorkerThread
    public void putAll(Map<? extends K, ? extends V> map) {
        Tasks.forResult(createAll(map));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WorkerThread
    public void putAll(SimpleArrayMap<? extends K, ? extends V> array) {
        Tasks.forResult(createAll(array));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelled(DatabaseError databaseError) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
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

    /**
     * ${@inheritDoc}
     */
    @Override
    public Task<Void> createAll(Map<? extends K, ? extends V> map) {
        return null;
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public V read(K key) {
        return null;
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public Task<V> update(K key, V value) {
        return null;
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public Task<V> delete(K key) {
        return null;
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public Task<Void> free() {
        return null;
    }

}
