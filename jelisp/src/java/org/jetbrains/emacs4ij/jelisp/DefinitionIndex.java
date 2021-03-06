package org.jetbrains.emacs4ij.jelisp;

import com.intellij.util.xmlb.annotations.MapAnnotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * DefinitionIndex is a wrapper for map which is an index for Emacs symbols definitions (in source code of Emacs).
 * It has public API for serialization and deserialization for index persistence.
 */
public final class DefinitionIndex {
  private Map<Identifier, IdLocation> myValue = new HashMap<>();

  /**
   * Don't use this setter! It is only for deserialization of persistent data.
   * @param index the map to be set as index value.
   */
  @Deprecated
  public void setValue(Map<Identifier, IdLocation> index) {
    myValue = index;
  }

  @MapAnnotation(surroundWithTag = false, entryTagName = "index", keyAttributeName = "id", valueAttributeName = "locations", surroundValueWithTag = false, surroundKeyWithTag = false)
  public Map<Identifier, IdLocation> getValue() {
    return myValue;
  }

  public boolean isEmpty() {
    return myValue.isEmpty();
  }

  void setWith (DefinitionIndex index) {
    myValue = index.getValue();
  }

  SortedMap<String, Long> get (Identifier key) {
    if (!containsKey(key))
      return null;
//      throw new IllegalStateException(key.getName());
    return myValue.get(key).getLocations();
  }

  boolean containsKey (Identifier id) {
    return myValue.containsKey(id);
  }

  Set<Map.Entry<Identifier,IdLocation>> entrySet() {
    return myValue.entrySet();
  }

  int size() {
    return myValue.size();
  }

  void put (Identifier id, SortedMap<String, Long> value) {
    myValue.put(id, new IdLocation(value));
  }
}
