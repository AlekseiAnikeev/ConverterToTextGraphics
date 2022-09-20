package ru.agentche.image;

/**
 * @author Aleksey Anikeev aka AgentChe
 * Date of creation: 11.07.2022
 */
public class ColorToChar implements TextColorSchema {
    @Override
    public char convert(int color) {
        return "#$@%*+-'".charAt(color / 32);
    }
}

