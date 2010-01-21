function testModel(mode)
% testModel   Test case for GeneNetWeaver
%
%   Usage: >> testModel(mode)
%             - mode {'normal', 'silent'}
%               In silent mode, no graphs are plotted.
%
% Requires the following files:
%
% test_noexpnoise_wildtype.tsv, test_noexpnoise_proteins_wildtype.tsv,
% test_noexpnoise_knockouts.tsv, test_noexpnoise_proteins_knockouts.tsv,
% test_noexpnoise_knockdowns.tsv, test_noexpnoise_proteins_knockdowns.tsv,
% test_noexpnoise_multifactorial.tsv,
% test_noexpnoise_proteins_multifactorial.tsv,
% test_multifactorial_perturbations.tsv,
% test_noexpnoise_knockouts_timeseries.tsv,
% test_noexpnoise_proteins_knockouts_timeseries.tsv,
% test_noexpnoise_knockdowns_timeseries.tsv,
% test_noexpnoise_proteins_knockdowns_timeseries.tsv,
% test_noexpnoise_timeseries.tsv, test_noexpnoise_proteins_timeseries.tsv,
% test_timeseries_perturbations.tsv
%
%
% Thomas Schaffter, Ph.D. Student
% Ecole Polytechnique Federale de Lausanne (EPFL)
% Laboratory of Intelligent Systems (LIS)
% CH-1015 Lausanne, Switzerland
% http://lis.epfl.ch/161219
%
% Daniel Marbach, Ph.D. Student
% Ecole Polytechnique Federale de Lausanne (EPFL)
% Laboratory of Intelligent Systems (LIS)
% CH-1015 Lausanne, Switzerland
% http://lis.epfl.ch/136698
%
% June 11, 2009


% close all the figures opened
close all;

% parse arguments
if nargin<1
   mode = 'silent';
end

numGenes = 7;

global a_G1 a_G2 a_G3 a_G4 a_G5 a_G6 a_G7;
global singleGenePerturbation singleGenePerturbationIndex;
restoreWildTypeActivations();


%% ------------------------------------------------------------------------
% TEST 1: Compare wild-type (ODE, no experimental noise)
% Load wild-type
restoreWildTypeActivations();
wildTypeLoaded = extractSsData('test_noexpnoise_wildtype.tsv');
wildTypeProtLoaded = extractSsData('test_noexpnoise_proteins_wildtype.tsv');
% Check that it is a steady-state
singleGenePerturbationIndex = 1;
singleGenePerturbation = 1;
x0 = [wildTypeLoaded wildTypeProtLoaded]';
dxdt = odefun(0, x0);
assertEqualZero(dxdt, 1e-6);

disp('Check steady-state wild-type: OK');


%% ------------------------------------------------------------------------
% TEST 2: Compare single-gene knockouts (ODE, no experimental noise)
% Load knockouts
restoreWildTypeActivations();
knockoutsLoaded = extractSsData('test_noexpnoise_knockouts.tsv');
knockoutsProtLoaded = extractSsData('test_noexpnoise_proteins_knockouts.tsv');
% Check that they are steady-states
singleGenePerturbation = 0;
for i = 1:size(knockoutsLoaded, 1)
    singleGenePerturbationIndex = i;
    x0 = [knockoutsLoaded(i,:) knockoutsProtLoaded(i,:)]';
    dxdt = odefun(0, x0);
    assertEqualZero(dxdt, 1e-4);
end

disp('Check steady-states single-knockout: OK');


%% ------------------------------------------------------------------------
% TEST 3: Compare single-gene knockdowns (ODE, no experimental noise)
% Load knockouts
restoreWildTypeActivations();
knockdownsLoaded = extractSsData('test_noexpnoise_knockdowns.tsv');
knockdownsProtLoaded = extractSsData('test_noexpnoise_proteins_knockdowns.tsv');
% Check that they are steady-states
singleGenePerturbation = 0.5;
for i = 1:size(knockdownsLoaded, 1)
    singleGenePerturbationIndex = i;
    x0 = [knockdownsLoaded(i,:) knockdownsProtLoaded(i,:)]';
    dxdt = odefun(0, x0);
    assertEqualZero(dxdt, 1e-4);
end

disp('Check steady-states single-knockdown: OK');


%% ------------------------------------------------------------------------
% TEST 4: Compare multifactorial steady-states (ODE, no experimental noise)
% Load multifactorial
multifactorialLoaded = extractSsData('test_noexpnoise_multifactorial.tsv');
multifactorialProtLoaded = extractSsData('test_noexpnoise_proteins_multifactorial.tsv');
perturbations = extractSsData('test_multifactorial_perturbations.tsv');
% Check that it is a steady-state
singleGenePerturbationIndex = 1;
singleGenePerturbation = 1;
for i = 1:size(multifactorialLoaded, 1)
    
    restoreWildTypeActivations();
    a_G1 = perturbBasalActivation(a_G1, perturbations(1, 1));
    a_G2 = perturbBasalActivation(a_G2, perturbations(1, 2));
    a_G3 = perturbBasalActivation(a_G3, perturbations(1, 3));
    a_G4 = perturbBasalActivation(a_G4, perturbations(1, 4));
    a_G5 = perturbBasalActivation(a_G5, perturbations(1, 5));
    a_G6 = perturbBasalActivation(a_G6, perturbations(1, 6));
    a_G7 = perturbBasalActivation(a_G7, perturbations(1, 7));

    x0 = [multifactorialLoaded(i,:) multifactorialProtLoaded(i,:)]';
    dxdt = odefun(0, x0);
    assertEqualZero(dxdt, 10);
end

disp('Check steady-states multifactorial: OK');


%% ------------------------------------------------------------------------
% TEST 5: Compare single-gene knockouts trajectories (ODE, no experimental noise)
% Load time-series
restoreWildTypeActivations();
[timescale timeSeriesLoaded] = extractTsData('test_noexpnoise_knockout_timeseries.tsv');
[timescale timeSeriesProtLoaded] = extractTsData('test_noexpnoise_proteins_knockout_timeseries.tsv');
numTimeSeries = size(timeSeriesLoaded,1) / numGenes;

singleGenePerturbation = 0;
% for every time series
for i = 0:numTimeSeries-1
    
    % the loaded data
    ts = timeSeriesLoaded(i*numGenes+1:(i+1)*numGenes,:);
    tsProt = timeSeriesProtLoaded(i*numGenes+1:(i+1)*numGenes,:);
    
    singleGenePerturbationIndex = i+1;
    
    % integrate to see if equal
    x0 = [ts(:,1); tsProt(:,1)];
    [test testProt] = integrateTestModel(timescale, x0);
    
    % remove perturbation after first half
    %restoreWildTypeActivations();
    
    test = test';
    testProt = testProt';
    
    assertEqualZero(test - ts, 1e-03);
    assertEqualZero(testProt - tsProt, 1e-03);
    
    % Graphs
    if strcmp(mode, 'silent') == 0
        
        figure;
        
        for c=1:numGenes
            subplot(2,4,c);

            hold on;
            plot(timescale,ts(c,:)', 'line', '.');
            plot(timescale,tsProt(c,:)', 'color', 'red', 'line', '.'); % GNW integration

            plot(timescale,test(c,:)');
            plot(timescale,testProt(c,:)', 'color', 'red'); % Matlab integration
            hold off;

            axis([0 timescale(size(timescale,1)) 0 1]);

            if c==1
                ylabel('point = GNW, line = Matlab');
            end
            set(gcf, 'Name', ['Trajectories steady-states single-knockout (' int2str(i) ')']);
        end
    end
end

disp('Check trajectories steadys-states single-knockout: OK');


%% ------------------------------------------------------------------------
% TEST 6: Compare single-gene knockdowns trajectories (ODE, no experimental noise)
% Load time-series
restoreWildTypeActivations();
[timescale timeSeriesLoaded] = extractTsData('test_noexpnoise_knockdown_timeseries.tsv');
[timescale timeSeriesProtLoaded] = extractTsData('test_noexpnoise_proteins_knockdown_timeseries.tsv');
numTimeSeries = size(timeSeriesLoaded,1) / numGenes;

singleGenePerturbation = 0.5;
% for every time series
for i = 0:numTimeSeries-1
    
    % the loaded data
    ts = timeSeriesLoaded(i*numGenes+1:(i+1)*numGenes,:);
    tsProt = timeSeriesProtLoaded(i*numGenes+1:(i+1)*numGenes,:);
    
    singleGenePerturbationIndex = i+1;
    
    % integrate to see if equal
    x0 = [ts(:,1); tsProt(:,1)];
    [test testProt] = integrateTestModel(timescale, x0);
    
    % remove perturbation after first half
    %restoreWildTypeActivations();
    
    test = test';
    testProt = testProt';
    
    assertEqualZero(test - ts, 1e-03);
    assertEqualZero(testProt - tsProt, 1e-03);
    
    % Graphs
    if strcmp(mode, 'silent') == 0
        
        figure;
        
        for c=1:numGenes
            subplot(2,4,c);

            hold on;
            plot(timescale,ts(c,:)', 'line', '.');
            plot(timescale,tsProt(c,:)', 'color', 'red', 'line', '.'); % GNW integration

            plot(timescale,test(c,:)');
            plot(timescale,testProt(c,:)', 'color', 'red'); % Matlab integration
            hold off;

            axis([0 timescale(size(timescale,1)) 0 1]);

            if c==1
                ylabel('point = GNW, line = Matlab');
            end
            set(gcf, 'Name', ['Trajectories steady-states single-knockdown (' int2str(i) ')']);
        end
    end
end

disp('Check trajectories steady-states single-knockdown: OK');


%% ------------------------------------------------------------------------
% TEST 7: Compare multifactorial trajectories (ODE, no experimental noise)
% Load time-series
[timescale timeSeriesLoaded] = extractTsData('test_noexpnoise_timeseries.tsv');
[timescale timeSeriesProtLoaded] = extractTsData('test_noexpnoise_proteins_timeseries.tsv');
perturbations = extractSsData('test_timeseries_perturbations.tsv');

numTimeSeries = size(timeSeriesLoaded,1) / numGenes;

singleGenePerturbation = 1;
singleGenePerturbationIndex = 1;
% for every time series
for i = 0:numTimeSeries-1

    restoreWildTypeActivations();
    a_G1 = perturbBasalActivation(a_G1, perturbations(i+1, 1));
    a_G2 = perturbBasalActivation(a_G2, perturbations(i+1, 2));
    a_G3 = perturbBasalActivation(a_G3, perturbations(i+1, 3));
    a_G4 = perturbBasalActivation(a_G4, perturbations(i+1, 4));
    a_G5 = perturbBasalActivation(a_G5, perturbations(i+1, 5));
    a_G6 = perturbBasalActivation(a_G6, perturbations(i+1, 6));
    a_G7 = perturbBasalActivation(a_G7, perturbations(i+1, 7));
    
    
    % the loaded data
    ts = timeSeriesLoaded(i*numGenes+1:(i+1)*numGenes,:);
    tsProt = timeSeriesProtLoaded(i*numGenes+1:(i+1)*numGenes,:); 
    
    % integrate to see if equal
    x0 = [ts(:,1); tsProt(:,1)];
    halfTime = timescale(1:(length(timescale)+1)/2);
    [test1 testProt1] = integrateTestModel(halfTime, x0);
    
    % remove perturbation after first half
    restoreWildTypeActivations();
    
    x0 = [test1(size(test1,1),:) testProt1(size(testProt1,1),:)]';
    halfTime = timescale((length(timescale)+1)/2:length(timescale));
    [test2 testProt2] = integrateTestModel(halfTime, x0);
    
    test = [test1; test2(2:size(test2,1),:)];
    testProt = [testProt1; testProt2(2:size(testProt2,1),:)];
    
    test = test';
    testProt = testProt';
    
    if (i == 0)
        assertEqualZero(test - timeSeriesLoaded(1:7,:), 1e-3);
        assertEqualZero(testProt - timeSeriesProtLoaded(1:7,:), 1e-3);
    elseif (i == 1)
        assertEqualZero(test - timeSeriesLoaded(8:14,:), 1e-3);
        assertEqualZero(testProt - timeSeriesProtLoaded(8:14,:), 1e-4);
    end
    
    % Graphs
    if strcmp(mode, 'silent') == 0
        
        figure;

        for c=1:numGenes
            subplot(2,4,c);

            hold on;
            plot(timescale,ts(c,:)', 'line', '.');
            plot(timescale,tsProt(c,:)', 'color', 'red', 'line', '.'); % GNW integration

            plot(timescale,test(c,:)');
            plot(timescale,testProt(c,:)', 'color', 'red'); % Matlab integration
            hold off;

            axis([0 timescale(size(timescale,1)) 0 1]);

            if c==1
                ylabel('point = GNW, line = Matlab');
            end
            set(gcf, 'Name', ['Trajectories multifactorial (' int2str(i) ')']);
        end
    end
end

disp('Check trajectories multifactorial: OK');
disp('CHECK COMPLETE !')

end



% =========================================================================
% SUBFUNCTIONS

% Check that the given values (vector or matrix) are equal zero with the
% given precision

function assertEqualZero(data, precision)

    for i = 1:size(data,1)
       for j = 1:size(data,2)
          if (data(i,j) < -precision || data(i,j) > precision)
          %if (abs(data(i,j)) > abs(precision))
              error(['Assert equal zero failed, data(' num2str(i) ',' num2str(j) ') = ' num2str(data(i,j))]);
          end
       end
    end
end

% -------------------------------------------------------------------------
% Set the wild-type alphas

function restoreWildTypeActivations()

    global a_G1 a_G2 a_G3 a_G4 a_G5 a_G6 a_G7;
    a_G1(1) = 0.6340394109041214;
    a_G1(2) = 1.0;
    a_G1(3) = 0.0;
    a_G1(4) = 0.5142640757922741;
    a_G2 = 1;
    a_G3 = 1;
    a_G4 = 1;
    a_G5 = 1;
    a_G6 = 1;
    a_G7 = 1;
end

% -------------------------------------------------------------------------
% Set the alphas according to the given perturbation

function alpha = perturbBasalActivation(alpha, deltaBasalActivation)

    n = size(alpha,2);
    
    if alpha(1)+deltaBasalActivation > 1
        deltaBasalActivation = 1-alpha(1);
    elseif alpha(1)+deltaBasalActivation < 0
        deltaBasalActivation = 0-alpha(1);
    end
    
    for i=1:n
        alpha(i) = alpha(i)+deltaBasalActivation;
        if alpha(i) < 0
            alpha(i) = 0;
        elseif alpha(i) > 1
            alpha(i) = 1;
        end
    end
end


% -------------------------------------------------------------------------

% String modifier
% "'Gene1'" -> 'Gene1'
function list = stringRemoveQuotes(input)
    s = size(input, 1);
    for i=1:s
        input(i) = regexprep(input(i), '"', '');
    end
    list = input;
end


% -------------------------------------------------------------------------

% Open the file associated to filename and return the stream.
function fin = openfile(filename)

    fin = fopen(filename,'r');
    if fin < 0
       error(['Could not open ',filename,' for input']);
    end
end

% -------------------------------------------------------------------------

% Read the file associated to the stream fin and extract all the experi-
% ments. The layout of the output data is a (M X TP) with:
% - M the total number of time series curves
% - TP is the number of time point in the time scale
%
% Hypothesis: All experiments have the same time scale.
function [timescale, data] = extractTsData(filename)


    fin = openfile(filename);
    [numGenes,labelsStr] = extractGeneLabels(fin);

    rawData = fscanf(fin,'%f'); % Load the numerical values into one long
                                % vector
    fclose(fin);                    
    
    % timescale and time-series data are mixed.
    % The output data will not contain the time scale. 
    [timescale, data] = extractTimeScale(rawData, numGenes);
    
    % the number of time series
    numTimeSeries = size(data,1) / numGenes;
    
    % Set the layout described above for data
    TP = size(timescale,1);
    M = size(data,1)/TP;
    data = reshape(data,TP,M)';
end

% -------------------------------------------------------------------------

% Read the file associated to the stream fin and extract all the experi-
% ments.
function data = extractSsData(filename)

    fin = openfile(filename);
    [numGenes, labelsStr] = extractGeneLabels(fin);
    numGenes = numGenes + 1; % extractGeneLabels was made for time-series
    data = fscanf(fin,'%f'); % Load the numerical values into one long
                             % vector
    fclose(fin);
    
    numExperiments = length(data)/numGenes;
    data = reshape(data,numGenes,numExperiments)';
end

% -------------------------------------------------------------------------

% Extract the time scale from the raw data and remove this same time scale
% from the experimental time series data.
% The time scale is only extract from the first experiment (see above
% hypothesis).
function [timescale, data] = extractTimeScale(rawData, numGenes)

    timescale = rawData(1,1);
    stop = 0;
    
    while stop == 0
        index = size(timescale,1)*(numGenes+1)+1;
        if (index <= size(rawData,1) && rawData(index,1) ~= 0)
            timescale = [timescale; rawData(index,1)];
        else
            stop = 1;
        end
    end
    data = removeTimeScale(rawData,numGenes);
    data = repartitionInCurves(data,numGenes,size(timescale,1));
end

% -------------------------------------------------------------------------

% Finally, data is a vector with time series curves one after one.
function correctData = repartitionInCurves(data,numGenes,numTimePoints)

    numExp = size(data,1)/(numGenes*numTimePoints);
    curve = zeros(numTimePoints,1);
    correctData = zeros(numTimePoints,1);
    
    for e=1:numExp
        expDataIndex = (e-1)*numGenes*numTimePoints+1;
        for g=1:numGenes
            geneDataIndex = expDataIndex+(g-1);
            for pt=1:numTimePoints
               pointDataIndex = geneDataIndex+(pt-1)*(numGenes);
               curve(pt) = data(pointDataIndex,1);
            end
            if e ==1 && g==1
               correctData = curve;
            else
               correctData = [correctData;curve];
            end
        end
    end
end

% -------------------------------------------------------------------------

% Remove the time scale of the raw time-series data.
function data = removeTimeScale(rawData,numGenes)

    numRows = size(rawData,1)/(numGenes+1);
    data = rawData(2:numGenes+1,1);
    
    for i=1:numRows-1
        start = i*(numGenes+1)+2;
        finish = start+numGenes-1;
        data = [data;rawData(start:finish,1)];
    end
end

% -------------------------------------------------------------------------

% Extract all the gene labels. Output is a cellstr array.
function [numGenes, labelsStr] = extractGeneLabels(fin)

    header = fgetl(fin); % Get the first line, the horizontal header
    maxlen = 0;
    stop = 0;
    numCol = 0;

    while stop == 0
      [next,header] = strtok(header); %  parse next column label
      nextLen = length(next);
      maxlen = max(maxlen,nextLen); %  find the longest so far
      if nextLen == 0
          stop = 1;
      else
         numCol = numCol+1;
      end
    end

    numGenes = numCol-1;
%     sprintf('Number of genes: %d', numGenes)

    labels = blanks(maxlen);
    frewind(fin); % rewind in preparation for actual reading of labels and data
    buffer = fgetl(fin); %  get next line as a string
    for j=1:numCol
      [next,buffer] = strtok(buffer); %  parse next column label
      n = j; %  pointer into the label array for next label
      labels(n,1:length(next)) = next; %  append to the labels matrix
    end

    % Conversion labels(char) -> labels(string)
    labelsStr = cellstr(labels);
    labelsStr = labelsStr(2:size(labelsStr,1),:);
    labelsStr = stringRemoveQuotes(labelsStr);
end


function [data dataProt] = integrateTestModel(timescale, x0)
    [T,Y] = ode45(@odefun,timescale,x0);
    
    data = Y(:,1:7);
    dataProt = Y(:,8:14);
end


function dxdt = odefun(t,x)
    global a_G2 a_G3 a_G4 a_G5 a_G6 a_G7;
    global singleGenePerturbation singleGenePerturbationIndex;
    
    % effective perturbation
    ep = ones(size(x));
    ep(singleGenePerturbationIndex) = singleGenePerturbation;
    
    deltaG1 = 0.0244593495713368;
    deltaP1 = 0.027029271685414604;

    deltaG2 = 0.032403917679442944;
    deltaP2 = 0.01955767207878401;
    
    deltaG3 = 0.018355052712367845;
    deltaP3 = 0.023751159048236645;
    
    deltaG4 = 0.024457762135688992;
    deltaP4 = 0.026713192906690518;
    
    deltaG5 = 0.02413740785440112;
    deltaP5 = 0.027595499817465636;
    
    deltaG6 = 0.026625243133319956;
    deltaP6 = 0.018924697221427155;
    
    deltaG7 = 0.030636694139202322;
    deltaP7 = 0.033925200280478095;
    
    dxdt = zeros(size(x));
    dxdt(1) = ep(1)*deltaG1*fG1(x(8:14)) - deltaG1*x(1);
    dxdt(2) = ep(2)*deltaG2*a_G2 - deltaG2*x(2);
    dxdt(3) = ep(3)*deltaG3*a_G3 - deltaG3*x(3);
    dxdt(4) = ep(4)*deltaG4*a_G4 - deltaG4*x(4); 
    dxdt(5) = ep(5)*deltaG5*a_G5 - deltaG5*x(5); 
    dxdt(6) = ep(6)*deltaG6*a_G6 - deltaG6*x(6); 
    dxdt(7) = ep(7)*deltaG7*a_G7 - deltaG7*x(7); 
    
    dxdt(8) = deltaP1*x(1) - deltaP1*x(8); 
    dxdt(9) = deltaP2*x(2) - deltaP2*x(9);     
    dxdt(10) = deltaP3*x(3) - deltaP3*x(10); 
    dxdt(11) = deltaP4*x(4) - deltaP4*x(11); 
    dxdt(12) = deltaP5*x(5) - deltaP5*x(12); 
    dxdt(13) = deltaP6*x(6) - deltaP6*x(13); 
    dxdt(14) = deltaP7*x(7) - deltaP7*x(14);
end


function f = fG1(x) 
    global a_G1;
    
    k_1 = 0.6435221230065996;
    k_2 = 0.18309229451561523;
    k_3 = 0.20874460125591632;
    k_4 = 0.31632696418928863;
    k_5 = 0.06414384596469654;
    k_6 = 0.129853630475479;
    n_1 = 3.686492278166225;
    n_2 = 1.639106037657112;
    n_3 = 3.5486143808360606;
    n_4 = 3.03211262038767;
    n_5 = 3.453637614074136;
    n_6 = 3.0429156191875575;
    
    y1 = power(x(5)/k_1, n_1);
    y2 = power(x(7)/k_2, n_2);    
    y3 = power(x(6)/k_3, n_3);    
    y4 = power(x(4)/k_4, n_4);
    y5 = power(x(3)/k_5, n_5);
    y6 = power(x(2)/k_6, n_6);
    
    % doesn't bind as complex
    P1 = y1*y2 / ((1+y1)*(1+y2)*(1+y3));
    % binds as complex
    P2 = y4*y5*y6 / (1 + y4*y5*y6);
    
    S0 = (1-P1)*(1-P2);
    S1 = P1*(1-P2);
    S2 = (1-P1)*P2;
    S3 = P1*P2;
    
    f = a_G1(1)*S0 + a_G1(2)*S1 + a_G1(3)*S2 + a_G1(4)*S3;
end


