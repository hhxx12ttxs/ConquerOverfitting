if (!(obj instanceof Timestamps))
return false;
Timestamps other = (Timestamps) obj;
if (getCreateTs() == null) {
if (other.getCreateTs() != null)
return false;
} else if (!getCreateTs().equals(other.getCreateTs()))

