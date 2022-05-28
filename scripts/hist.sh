baseurl="https://api-pub.bitfinex.com/v2/tickers/hist?symbols="
tickers="tBTCUSD,tETHBTC,tXRPBTC,tADABTC,tSOLBTC,tDOGBTC,tDOTBTC,tDAIBTC,tIOTBTC,tXMRBTC"
echo "Getting tickers "$tickers

nowstr=`date +%s%3N`
declare -i nowtimestamp=$((nowstr))
declare -i yearago=$((nowtimestamp-365*24*3600*1000))
declare -i starttimestamp=$yearago
declare -i endtimestamp=$starttimestamp
echo $endtimestamp" < "$nowtimestamp
while (($endtimestamp < $nowtimestamp))
do
  endtimestamp=$starttimestamp+$((20*10000))
  echo "Calculated endtimestamp "$endtimestamp" ("$((`date +%s%3N`-$endtimestamp))" left)"

  timebounds="&start="$starttimestamp"&end="$endtimestamp
  url=$baseurl$tickers$timebounds"&limit=200"
  echo "Requesting to "$url
  data=$(curl $url)

  regex=\\\[.+\\\]
  
  if [[ "$data" =~ $regex ]];
  then
    filename="../data/"$starttimestamp"-"$endtimestamp".csv"
    echo "Saving into "$filename
    echo $data > $filename
	"Got data"$data
  else
    echo "Blank response"
  fi
  
  sleep 1
  starttimestamp=$(($endtimestamp+10000))
done
