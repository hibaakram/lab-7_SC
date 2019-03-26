package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConcreteEdgesGraph<L> implements Graph<L> {
    
    private final Set<L> vertices = new HashSet<>();
    private final List<Edge<L>> edges = new ArrayList<>();
    
   /* Abstraction function:
    *   This function represent edges, vertices in a graph
    *   and a target direction that carries weight 
    * Representation invariant:
    *   Vertices are objects of type L
    *   Edges is a list made of weighted edges and paired vertices
    *
    * Safety from rep exposure:
    *   Most of the variables are private and final
    *   vertices and edges are mutable types
    */
    
    public ConcreteEdgesGraph(){
    }
    private void checkRep(){
        final int sizeOfEdges = edges.size();
        final int sizeOfVertices = vertices.size();
        int minNumberOfVertices = 
                sizeOfEdges == 0 ? 0 : (int)Math.ceil(Math.sqrt(2 * sizeOfEdges) + 0.5);
        
        assert sizeOfVertices >= minNumberOfVertices;  
    }
    /** Returns true if vertex label is added*/
    @Override public boolean add(L vertex) {
        return vertices.add(vertex);
    }    
    @Override public int set(L source, L target, int weight) {
        assert weight >= 0;
        
        int indexOfEdge = indexOfEdgeInEdges(source, target);
        int previousWeight = 0;
        final Edge<L> previousEdge;
        
        if (weight > 0) {
            Edge<L> newEdge = new Edge<>(source, target, weight);
            if ( indexOfEdge < 0 ) {
                add(source);
                add(target);
                edges.add(newEdge);
            } else {
                previousEdge = edges.set(indexOfEdge, newEdge);
                previousWeight = previousEdge.getWeight();
            }
        } else if ( weight == 0 && indexOfEdge >= 0) {
            previousEdge = edges.remove(indexOfEdge);
            previousWeight = previousEdge.getWeight();
        }
        checkRep();
        return previousWeight;
    }
    private int indexOfEdgeInEdges(L source, L target){        
        for(int i = 0;  i < edges.size(); i++){
            Edge<L> edge = edges.get(i);
            if (edge.getSource().equals(source) &&
                    edge.getTarget().equals(target)){
                return i;
            }
        }
        return -1;
    }
    
    
    @Override public boolean remove(L vertex) {
        final int initialSizeEdges = edges.size();
        final int initialSizeVertices = vertices.size();
        
        Predicate<Edge<L>> vertexInEdge = (Edge<L> edge) -> 
              ( ( edge.getSource().equals(vertex) ) ||
                ( edge.getTarget().equals(vertex) ) ) ;
        Predicate<L> vertexInVertices = v -> v.equals(vertex);
        
        boolean removedEdge = edges.removeIf(vertexInEdge);
        boolean removedVertice = vertices.removeIf(vertexInVertices);
        
        //NB a vertex can exist without being in an edge
        //if removedEdge, then removedVertice
        if(removedVertice){
            assert initialSizeVertices != vertices.size();
            assert initialSizeVertices - 1 == vertices.size();
        }
        if(removedEdge){
            assert initialSizeEdges != edges.size();
            assert removedVertice;
        }
        checkRep();
        return initialSizeVertices - 1 == vertices.size();
    }
    /** Returns an read-only view of this ConcreteEdgesGraph's vertices */
    @Override public Set<L> vertices() {
        return Collections.unmodifiableSet(vertices);
    }

    /** Returns a map of a target's sources */
    @Override public Map<L, Integer> sources(L target) {
        return edges.stream()
                .filter(edge -> edge.getTarget().equals(target))
                .collect(Collectors.toMap(Edge::getSource, Edge::getWeight));
    }
    /** Returns a map of a source's targets */
    @Override public Map<L, Integer> targets(L source) {
        return edges.stream()
                .filter(edge -> edge.getSource().equals(source))
                .collect(Collectors.toMap(Edge::getTarget, Edge::getWeight));
    }
    @Override public String toString(){
        if ( edges.isEmpty() ) {
            return "Empty Graph";
        }
        return edges.stream()
                .map(edge -> edge.toString())
                .collect(Collectors.joining("\n"));
    }
}
/**
 * Immutable type that represents an edge in a graph.
 * 
 * This class is internal to the rep of ConcreteEdgesGraph.
 * 
 * <p>PS2 instructions: the specificatverticesion and implementation of this class is
 * up to you.
 */
class Edge<L>{
    private final L source;
    private final L target;
    private final int weight;
   /* Abstraction function:
    *   This function represents the edge connecting from source to target
    * Representation invariant:
    *   source is a non-null L, target is a non-null L
    *   L must be immutable, weight > 0
    * Safety from rep exposure:
    *   All fields are private and final
    *   source and target are of type L, required to be immutable
    *   int is also immutable.
    */   setWeight() creates a new Edge object
    
    public Edge(final L source, final L target, final int weight){
        assert weight > 0;
        
        this.source = source;
        this.target = target;
        this.weight = weight;
        checkRep();
    }
    private void checkRep(){
        assert source != null;
        assert target != null;
        assert weight > 0;
    }
    //observers
    /** Returns this Edge's source*/   
    public L getSource(){
        return source;
    }
    /**Returns this Edge's target*/
    public L getTarget(){
        return target;
    }
    /**Returns this Edge's weight*/
    public int getWeight(){
        return weight;
    }
    
    //producers
    /**
     * Changes the weight of this Edge
     * 
     * @param newWeight an int, requires newWeight > 0
     * @return a new Edge with newWeight 
     */
    public Edge<L> setWeight(int newWeight){
        checkRep();
        return new Edge<>(source, target, newWeight);
    }
    /** Returns th string representation of a weighted edge
     * 
     * An edge is made up of two vertices, so the rep
     * should contain the source vertex and the target vertex
     * that make the edge, including its weight
     * 
     * @return String containing source, target and weight of this edge
     *         with the following structure:
     *              getSource() -> getTarget(): getWeight()
     */    
    @Override public String toString(){
        return getSource().toString() + 
                " -> " + 
                getTarget().toString() + 
                ": " + 
                getWeight();
    }
    /** Checks if two Edge objects are equal
     * @param that object to compare
     * @return true if this.source = that.source and
     *                 this.target = that.target and
     *                 this.weight = that.weight
     *         comparison is case-insensitive
     */
    @Override public boolean equals(Object that){
        if (! (that instanceof Edge)) {
            return false;
        }
        Edge<?> thatEdge = (Edge<?>)that;
        return this.getSource().equals(thatEdge.getSource()) &&
               this.getTarget().equals(thatEdge.getTarget()) &&
               this.getWeight() == thatEdge.getWeight();
    }
    @Override public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + getSource().hashCode();
        result = prime * result + getTarget().hashCode();
        result = prime * result + (int) getWeight();
        return result;
    }
}
