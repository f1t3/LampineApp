function f = crc16(esnostart, esnostop, nsim)

reset = 0

WordLen = 16;

for j = esnostart+201 : 1 : esnostop+201
      
results = loadres('resultscrc16.mat');
esnodb = results.esnodb(j);  

% Derivatives
Es = 0.5;
EsN0 = 10^(esnodb/10);
N0 =  Es/(EsN0);
    
nerractual = 0;
nerrdet = 0;
nerrmiss = 0;
nerrfalse = 0;

if reset == 1
    rescrc.nsim       = zeros(1,numel(rescrc.esnodb));
    rescrc.nerractual = zeros(1,numel(rescrc.esnodb));
    rescrc.nerrdet     = zeros(1,numel(rescrc.esnodb));
    rescrc.nerrmissed = zeros(1,numel(rescrc.esnodb));
    rescrc.nerrfalse  = zeros(1,numel(rescrc.esnodb));
end

% CRC-8: Ignore highest 1
%gen  = [1, 1, 0, 1, 0, 0, 1, 1, 1]; % CRC-8-Bluetooth
%gen  = [1, 0, 1, 0, 1, 1, 1, 0, 1]; % x8 + x7 + x4 + x0
gen  = [1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]; % x8 + x7 + x4 + x0

tic 

for i = 1:nsim
    % Generate random word
    x = randi([0,1], 1, WordLen * 8);
    % Generate checksums
    crc = calccrc([x, zeros(1,numel(gen)-1)],gen);
    x = [x, crc];
    % Generate noise vector 
    w = sqrt(N0/2) .* randn(1, WordLen * 8 + numel(gen)-1);
    % TX signal
    s = x + w;
    % Hard decoding
    y = (s > 0.5);
    % Calculate checksums
    crc = calccrc(y, gen);
    % Check if actual word error occured (only in data!)
    e = sum(sum((y(1:WordLen*8) ~= x(1:WordLen*8)),2),1);
    e = e > 0;
    nerractual = nerractual + e;
    % Check CRC, check if word error would be detected
    eCS = sum(crc) ~= 0;
    nerrdet = nerrdet + eCS;
    % Check if error would be missed
    nerrmissed = nerrmissed + (eCS < e);
    % Check if false error
    nerrfalse = nerrfalse   + (eCS > e);
end

T = toc;

storeres('resultscrc16', results, esnodb, nsim, nerractual, nerrdet, nerrmiss, nerrfalse)

end

f = T/nsim;

end

function f = calccrc(word, gen)
    gentmp = [gen, zeros(1, numel(word)-numel(gen))];
    for bit = 1 : numel(word) - numel(gen) + 1
        if word(bit) ~= 0
            % MSB is 1
            word = xor(word,gentmp);
        end 
        gentmp(2:numel(gentmp)) = gentmp(1:numel(gentmp)-1);
        gentmp(1) = 0;
    end
    f = word(numel(word)-(numel(gen)-1)+1:numel(word));
end

