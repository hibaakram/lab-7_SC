package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConcreteVerticesGraph<L> implements Graph<L> {
    private final List<Vertex<L>> vertices = new ArrayList<>();
    
    // Abstraction function:
    //   this represents a graph which is directed and weighted where a number of different
    //   different vertices are connected as weighted source-target pairs 

    // Representation invariant:
    //   In vertices a maximum of one vertex can exist
   
    // Safety from rep exposure:
    //   we have a mutable list vertices so the operation makes defensive copies and
    //   uses immutable views in order to avoid sharing the rep
    //   Vertex is also a mutable type and operations use defensive copies to avoid sharing the rep
  
    public ConcreteVerticesGraph(){
    }
    private void checkRep(){        
        assert vertices().size() == vertices.size();
    }
    private int indexInVertices(L label){
        for(int i = 0; i < vertices.size(); i++){
            if ( vertices.get(i).getLabel().equals(label) ) {
                return i;
            }
        }
        return -1;
    }

    @Override public boolean add(L vertex) {        
        if ( vertices().contains(vertex) ) {
            return false;
        }
        Vertex<L> vertexObj = new Vertex<>(vertex);    
        final boolean vertexAdded = vertices.add(vertexObj);
        checkRep();
        return vertexAdded;
    }
    
    @Override public int set(L source, L target, int weight) {
        assert source != target;
        assert weight >= 0;
        
        final Vertex<L> sourceVertex;
        final Vertex<L> targetVertex;
        
        Set<L> verticeLabels = vertices();
        if ( verticeLabels.contains(source) ) {
            int sourceIndex = indexInVertices(source);
            sourceVertex = vertices.get(sourceIndex);
        } else {
            sourceVertex = new Vertex<>(source);
            vertices.add(sourceVertex);
        }
        
        if ( verticeLabels.contains(target) ) {
            int targetIndex = indexInVertices(target);
            targetVertex = vertices.get(targetIndex);
        } else {
            targetVertex = new Vertex<>(target);
            vertices.add(targetVertex);
        }
        int sourcePrevWeight = sourceVertex.setTarget(target, weight);
        int targetPrevWeight = targetVertex.setSource(source, weight);
        assert sourcePrevWeight == targetPrevWeight;
        checkRep();
        return sourcePrevWeight;
    }
    
    @Override public boolean remove(L vertex) {
        if ( !( vertices().contains(vertex)) ) {
            return false;
        }
        int vertexIndex = indexInVertices(vertex);
        assert vertexIndex != -1;
        final Vertex<L> removedVertex = vertices.remove(vertexIndex);
        assert removedVertex.getLabel() == vertex;  
        for( Vertex<L> v: vertices ) {
            v.remove(vertex);
        }
        return removedVertex != null;
    }
    @Override public Set<L> vertices() {
        return vertices.stream()
                .map(Vertex::getLabel)
                .collect(Collectors.toSet());
    }
    @Override public Map<L, Integer> sources(L target) {
        final int targetIndex = indexInVertices(target);
        if ( targetIndex < 0 ) {
            return Collections.emptyMap();
        }
        Vertex<L> targetVertex = vertices.get(targetIndex);
        return Collections.unmodifiableMap(targetVertex.getSources());
    }
   
    @Override public Map<L, Integer> targets(L source) {
        final int sourceIndex = indexInVertices(source);
        if ( sourceIndex < 0 ) {
            return Collections.emptyMap();
        }
        Vertex<L> sourceVertex = vertices.get(sourceIndex); 
        return Collections.unmodifiableMap(sourceVertex.getTargets());
    }
    //TODO toString()
    @Override public String toString(){
        return vertices.stream()
                .filter(vertex -> vertex.getTargets().size() > 0)
                .map(vertex -> vertex.getLabel().toString() + " -> " + vertex.getTargets())
                .collect(Collectors.joining("\n"));
    }
}
class Vertex<L> {
    private final L label;
    private final Map<L, Integer> sources = new HashMap<>();
    private final Map<L, Integer> targets = new HashMap<>();
    // Abstraction Function:
    //   This shows a vertex connected to other vertices in the graph as either a source or a target
    //   An edge is represented by a weighted connection between 2 vertices and the direction is determined by which vertx 
    //   is a source and which one is a target
    //
    // Representation Invariant:
    //   vertex must be immutable
    //   vertex must never be its own source or target
    //   sources and targets must be unique vertices
    //   the weight of a connection must be greater than 0
    //
    // Safety from Exposure:
    //   All fields are private and final
    //   lable L is immutable
    //   defensive copies are used by operations as sources and targets are mutable
    //   and immutable views are used to prevent sharing the rep objects
   
    public Vertex(final L label){
        this.label = label;        
    }
    private void checkRep(){
        final Set<L> sourceLabels = sources.keySet();
        final Set<L> targetLabels = targets.keySet();
        
        assert !sourceLabels.contains(this.label);
        assert !targetLabels.contains(this.label);
    }
    private void checkInputLabel(final L inputLabel){
        assert inputLabel != null;
        assert inputLabel != this.label;
    }
    
    public L getLabel(){
        return this.label;
    }
    public boolean addSource(final L source, final int weight){
        checkInputLabel(source);
        assert weight > 0;
        
        if ( sources.putIfAbsent(source, weight) == null ){
            checkRep();
            return true;
        }
        return false;
    }
    public boolean addTarget(final L target, final int weight){
        checkInputLabel(target);
        assert weight > 0;
        
        if ( targets.putIfAbsent(target, weight) == null ) {
            checkRep();
            return true;
        }
        return false;
    }
    public int remove(final L vertex) {
        checkInputLabel(vertex);
        int sourcePrevWeight = removeSource(vertex);
        int targetPrevWeight = removeTarget(vertex);
        
        if ( sourcePrevWeight > 0 && targetPrevWeight > 0 ) {
            assert sourcePrevWeight == targetPrevWeight;
        }
        return sourcePrevWeight == 0 ? targetPrevWeight : sourcePrevWeight;
    }
    public int removeSource(final L source){
        checkInputLabel(source);
        
        Integer previousWeight = sources.remove(source);
        
        checkRep();
        return previousWeight == null ? 0 : previousWeight;
    }
    public int removeTarget(final L target){
        checkInputLabel(target);
        
        Integer previousWeight = targets.remove(target);
        
        checkRep();
        return previousWeight == null ? 0 : previousWeight;
    }
   
    public int setSource(final L source, final int weight){
        checkInputLabel(source);
        assert weight >= 0;
        final int previousWeight;
        
        if ( weight == 0 ) {
            previousWeight = removeSource(source); 
        } else if ( addSource(source, weight) || sources.get(source) == (Integer)weight) {
            previousWeight = 0;
        } else {
            previousWeight = sources.replace(source, weight);
        }
        checkRep();
        return previousWeight;
    }
    public int setTarget(final L target, final int weight){
        checkInputLabel(target);
        assert weight >= 0;
        final int previousWeight;
        
        if ( weight == 0 ) {
            previousWeight = removeTarget(target);
        } else if ( addTarget(target, weight) || targets.get(target) == (Integer)weight ) {
            previousWeight = 0;
        } else {
            previousWeight = targets.replace(target, weight);
        }
        checkRep();
        return previousWeight;
    }
    public Map<L, Integer> getSources(){
        return Collections.unmodifiableMap(sources);
    }
    /** Returns an immutable view of this vertex's targets*/
    public Map<L, Integer> getTargets(){
        return Collections.unmodifiableMap(targets);
    }
    public boolean isTarget(final L vertex){
        return targets.containsKey(vertex);
    }
    public boolean isSource(final L vertex){
        return sources.containsKey(vertex);
    }
    @Override public String toString(){
        return String.format(
                "%s -> %s \n" +
                "%s <- %s",
                this.label.toString(), this.targets,
                this.label.toString(), this.sources);
    }
}
