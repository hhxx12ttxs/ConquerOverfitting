public IcyBufferedImage getImage( final int t, final int z )
{
if ( previousT != t || previousZ != z )
{
for ( @SuppressWarnings( &quot;rawtypes&quot; )
final IterableIntervalProjector2D projector : projectors )
public VolumetricImage getVolumetricImage( final int t )
{
if ( t != previousT )
{
for ( @SuppressWarnings( &quot;rawtypes&quot; )

