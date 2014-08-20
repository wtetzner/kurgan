package org.bovinegenius.kurgan.types;

import org.bovinegenius.kurgan.ConfigException;

public class UnboundTypeParamException extends ConfigException {
    public UnboundTypeParamException(String message, Throwable cause) { super(message, cause); }
    public UnboundTypeParamException(String message) { super(message); }
    public UnboundTypeParamException(Throwable cause) { super(cause); }
}
