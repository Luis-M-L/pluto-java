declare -i nowtimestamp=10
echo nowtimestamp:$nowtimestamp
declare -i yearago=$((nowtimestamp-10))
echo yearago:$yearago
declare -i starttimestamp=$yearago
echo starttimestamp:$starttimestamp
declare -i endtimestamp=$starttimestamp
echo endtimestamp:$endtimestamp
while (($endtimestamp < $nowtimestamp))
do
  endtimestamp=$starttimestamp+$((1))
  echo "Calculated endtimestamp "$endtimestamp

  timebounds="&start="$starttimestamp"&end="$endtimestamp
  url=$baseurl$tickers$timebounds"&limit=200"
  echo "Requesting to "$url
  
  starttimestamp=$(($endtimestamp+1))
done
