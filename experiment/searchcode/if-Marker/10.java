return false;
if (getClass() != obj.getClass())
return false;
MarkerUtil other = (MarkerUtil) obj;
if (marker == null) {
if (other.marker != null)
return false;
} else if (!marker.equals(other.marker))
return false;
return true;
}

}

