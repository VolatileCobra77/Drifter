package ca.volatilecobra;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;

import java.lang.reflect.Method;
import java.util.function.Function;

public class GUIs {

    private Node guiNode;

    public GUIs(Node guiNode){
        this.guiNode = guiNode;
    }

    public Node getGuiNode() {
        return guiNode;
    }

    public void addObject(Container object){
        guiNode.attachChild(object);
    }

    public void removeObject(Container object){
        guiNode.detachChild(object);
    }

    public void addTextToObject(String text, Vector3f position, ColorRGBA color, Container ParentObject){
        Container parent = (Container)guiNode.getChild(ParentObject.getName());
        Label textLabel = new Label(text);
        textLabel.setColor(color);
        textLabel.setLocalTranslation(position);
        parent.attachChild(textLabel);
    }
    public void addTextToObject(String text, Vector3f position, ColorRGBA color, Node ParentObject){
        Container parent = (Container)guiNode.getChild(ParentObject.getName());
        Label textLabel = new Label(text);
        textLabel.setColor(color);
        textLabel.setLocalTranslation(position);
        parent.attachChild(textLabel);
    }
    public void addTextToObject(String text, Vector3f position, ColorRGBA color){
        Container parent = (Container)guiNode;
        Label textLabel = new Label(text);
        textLabel.setColor(color);
        textLabel.setLocalTranslation(position);
        parent.attachChild(textLabel);
    }
    public void addButtonToObject(String text, Vector3f position, ColorRGBA color, Container ParentObject, Runnable clickEvent){
        Container parent = (Container)guiNode.getChild(ParentObject.getName());
        Button button = new Button(text);
        button.setColor(color);
        button.setLocalTranslation(position);
        button.addClickCommands(e -> clickEvent.run());
        parent.attachChild(button);
    }



}
