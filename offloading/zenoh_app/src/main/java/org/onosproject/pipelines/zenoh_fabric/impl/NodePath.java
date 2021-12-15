package org.onosproject.pipelines.zenoh_fabric.impl;


import org.onosproject.net.Link;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Iterator;

public class NodePath{
    private ArrayList<Link> path_links;
    private int path_cost = Integer.MAX_VALUE;




    public NodePath() {
        path_links = new ArrayList<>();
    }


    public NodePath(ArrayList<Link> path_links, int path_cost) {
        this.path_links = path_links;
        this.path_cost = path_cost;
    }

    public ArrayList<Link> getNodePath_links() {
        return this.path_links;
    }

    public void setNodePath_links(ArrayList<Link> path_links) {
        this.path_links = path_links;
    }

    public void addLink(Link l){
        this.path_links.add(l);
    }

    public int getNodePath_cost() {
        return this.path_cost;
    }

    public void setNodePath_cost(int path_cost) {
        this.path_cost = path_cost;
    }

    public NodePath path_links(ArrayList<Link> path_links) {
        setNodePath_links(path_links);
        return this;
    }

    public NodePath path_cost(int path_cost) {
        setNodePath_cost(path_cost);
        return this;
    }


    public ArrayList<Link> copyNodePath(){
        ArrayList<Link> cloneNodePathList = new ArrayList<>();

        Iterator<Link> iterator = this.path_links.iterator();

        while(iterator.hasNext())
        {
            //Add the object clones
            cloneNodePathList.add((Link) iterator.next());
        }


        return cloneNodePathList;
    }




    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof NodePath)) {
            return false;
        }
        NodePath path = (NodePath) o;
        return Objects.equals(path_links, path.path_links) && path_cost == path.path_cost;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path_links, path_cost);
    }

    @Override
    public String toString() {
        return "{" +
            " path_links='" + getNodePath_links() + "'" +
            ", path_cost='" + getNodePath_cost() + "'" +
            "}";
    }
}
