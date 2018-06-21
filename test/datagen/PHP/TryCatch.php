<?php
try {
	echo inverse(5) . "\n";
} catch (Exception $e) {
	echo 'Caught exception: ',  $e->getMessage(), "\n";
} finally {
	echo "First finally.\n";
}
?>